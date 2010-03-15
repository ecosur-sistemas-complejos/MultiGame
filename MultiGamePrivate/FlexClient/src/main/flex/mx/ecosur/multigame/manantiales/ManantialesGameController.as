//copyright

package mx.ecosur.multigame.manantiales
{

    import flash.events.MouseEvent;

    import flash.geom.Point;

    import mx.collections.ArrayCollection;
    import mx.containers.Panel;
    import mx.controls.Alert;
    import mx.controls.Button;
    import mx.core.DragSource;
    import mx.core.IFlexDisplayObject;
    import mx.ecosur.multigame.component.BoardCell;
    import mx.ecosur.multigame.component.Token;
    import mx.ecosur.multigame.entity.ChatMessage;
    import mx.ecosur.multigame.entity.GameGrid;
    import mx.ecosur.multigame.entity.GamePlayer;
    import mx.ecosur.multigame.enum.Color;
    import mx.ecosur.multigame.enum.ExceptionType;
    import mx.ecosur.multigame.enum.MoveStatus;
    import mx.ecosur.multigame.manantiales.entity.CheckCondition;
    import mx.ecosur.multigame.manantiales.entity.Ficha;
    import mx.ecosur.multigame.manantiales.entity.ManantialesGame;
    import mx.ecosur.multigame.manantiales.entity.ManantialesMove;
    import mx.ecosur.multigame.manantiales.entity.ManantialesPlayer;
    import mx.ecosur.multigame.manantiales.entity.Suggestion;
    import mx.ecosur.multigame.manantiales.enum.ConditionType;
    import mx.ecosur.multigame.manantiales.enum.ManantialesEvent;
    import mx.ecosur.multigame.manantiales.enum.Mode;
    import mx.ecosur.multigame.manantiales.enum.TokenType;
    import mx.ecosur.multigame.manantiales.token.*;
    import mx.ecosur.multigame.util.MessageReceiver;
    import mx.effects.AnimateProperty;
    import mx.events.CloseEvent;
    import mx.events.DragEvent;
    import mx.events.DynamicEvent;
    import mx.events.EffectEvent;
    import mx.managers.DragManager;
    import mx.managers.PopUpManager;
    import mx.messaging.messages.ErrorMessage;
    import mx.messaging.messages.IMessage;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;
    import mx.rpc.remoting.RemoteObject;

    public class ManantialesGameController
    {

        // visual components
        public var _gameWindow:ManantialesWindow;

        private var _tokenStorePanels:ArrayCollection;
        private var _annCondGen:AnnualConditionsGenerator;
        private var _alerts:ArrayCollection;
        private var _endAlert:GraphicAlert;
        private var _stageChangeAlert:GraphicAlert;
        private var _suggestionHandler:SuggestionHandler;

        // data objects
        public var _currentPlayer:ManantialesPlayer;
        public var _game:ManantialesGame;
        public var _moves:ArrayCollection;        
        private var _players:ArrayCollection;
        private var _gameId:int;
        private var _selectedMoveInd:Number;

        // server objects
        public var _gameService:RemoteObject;
        private var _msgReceiver:MessageReceiver;

        // flags
        private var _isMoving:Boolean;
        private var _executingMove:ManantialesMove;
        private var _isTurn:Boolean;
        private var _isEnded:Boolean;
        private var _previousToken:Token;

        // constants
        private static const MESSAGING_DESTINATION_NAME:String = "multigame-destination";
        private static const GAME_SERVICE_DESTINATION_NAME:String = "gameService";
        private static const GAME_SERVICE_GET_GRID_OP:String = "getGameGrid";
        private static const GAME_SERVICE_GET_PLAYERS_OP:String = "getPlayers";
        private static const GAME_SERVICE_GET_MOVES_OP:String = "getMoves";
        private static const GAME_SERVICE_DO_MOVE_OP:String = "doMove";
        

        public function ManantialesGameController (gameWindow:ManantialesWindow)
        {

            // set references
            _gameWindow = gameWindow;
            _currentPlayer = ManantialesPlayer (gameWindow.currentPlayer);
            _game = ManantialesGame(gameWindow.currentGame);
            _gameId = _game.id;

                // instantiate collections
            _moves = new ArrayCollection();
            _tokenStorePanels = new ArrayCollection();
            _alerts = new ArrayCollection();

             // initialize game service remote object
            _gameService = new RemoteObject();
            _gameService.destination = GAME_SERVICE_DESTINATION_NAME;
            _gameService.addEventListener(ResultEvent.RESULT,
                gameServiceResultHandler);
            _gameService.addEventListener(FaultEvent.FAULT,
                gameServiceFaultHandler);

            // initialize message receiver
            _msgReceiver = new MessageReceiver(MESSAGING_DESTINATION_NAME, _game.id);
            _msgReceiver.addEventListener(MessageReceiver.PROCESS_MESSAGE, processMessage);

            // initialize game status
            _gameWindow.gameStatus.showMessage("Welcome to the game " +
                _currentPlayer.registrant.name + "!\n\n You are " +
                Color.getColorDescription(_currentPlayer.color),
                Color.getColorCode(_currentPlayer.color));

            // initialize the board
            _gameWindow.board.dragEnterHandler = dragEnterBoardCell;
            _gameWindow.board.dragDropHandler = dragDropCell;
            _gameWindow.board.dragExitHandler = dragExitCell;

            // initialize the move viewer
            _gameWindow.moveViewer.addEventListener(MoveViewer.MOVE_EVENT_GOTO_MOVE, gotoMove);
            _gameWindow.moveViewer.board = _gameWindow.board;


            // get the game grid, players and moves
            var callGrid:Object = _gameService.getGameGrid(_gameId);
            callGrid.operation = GAME_SERVICE_GET_GRID_OP;
            var callPlayers:Object = _gameService.getPlayers(_gameId);
            callPlayers.operation = GAME_SERVICE_GET_PLAYERS_OP;
            var callMoves:Object = _gameService.getMoves(_gameId);
            callMoves.operation = GAME_SERVICE_GET_MOVES_OP;

            _suggestionHandler = new SuggestionHandler (this);
        }

        public function initializeTokenStorePanel (tokenStorePanel:Panel):void {
            var _store:ManantialesTokenStore = ManantialesTokenStore(tokenStorePanel.getChildAt(0));
            _store.startMoveHandler = startMove;
            _store.endMoveHandler = endMove;
            _store.visible=true;
            _store.active=true;
        }

        public function dragEnterBoardCell(evt:DragEvent):void{

            if (evt.dragSource.hasFormat("token")){

                var token:ManantialesToken = ManantialesToken(evt.dragSource.dataForFormat("token"));
                var boardCell:RoundCell = RoundCell(evt.currentTarget);
                boardCell.addEventListener(DragEvent.DRAG_EXIT, dragExitCell);

                _previousToken = boardCell.token;

                // calculate if move is valid
                if (validateMove(boardCell, token)){
                    boardCell.select(token.cell.colorCode);
                    DragManager.acceptDragDrop(boardCell);
                }
            }
        }

        private function validateMove(boardCell:RoundCell, token:ManantialesToken):Boolean
        {
            var ret:Boolean = false;
            if (boardCell.token is UndevelopedToken) {
                if (token.cell.color == boardCell.color) {
                        ret = true;
                    } else if (boardCell.color == Color.UNKNOWN)
                /* Special case, border zones. Cell color is
                 * calculated by RoundCell except for this case */

                switch (token.cell.color) {
                    case Color.YELLOW:
                        ret = (boardCell.column < 5 && boardCell.row < 5);
                        break;
                    case Color.BLUE:
                        ret = (boardCell.column < 5 && boardCell.row > 3);
                        break;
                    case Color.RED:
                        ret = (boardCell.column > 3 && boardCell.row < 5);
                        break;
                    case Color.BLACK:
                        ret = (boardCell.column > 3 && boardCell.row > 3);
                        break;
               }
            } else {
                if (boardCell.token.cell.color == token.cell.color) {

                    /* Replacement Logic */
                    ret = (boardCell.token is ForestToken && token is ModerateToken ||
                           boardCell.token is ModerateToken &&
                                  (token is ForestToken || token is IntensiveToken) ||
                           boardCell.token is ViveroToken && token is SilvopastoralToken);
                }
            }

            return ret;
        }

        public function startMove(evt:MouseEvent):void{

            if (!_isMoving && _isTurn){

                // initialize drag source
                var token:ManantialesToken = ManantialesToken(evt.currentTarget);
                var ds:DragSource = new DragSource();
                ds.addData(token, "token");

                // create proxy image and start drag
                var dragImage:IFlexDisplayObject = token.createDragImage();
                DragManager.doDrag(token, ds, evt, dragImage);
                _isMoving = true;

            }
        }

        public function endMove(evt:DragEvent):void{

            if (evt.dragSource.hasFormat("token")){
                _isMoving = false;
                var token:ManantialesToken = ManantialesToken(evt.currentTarget);
                token.selected = false;
            }
        }


        /* Go directly to a given move in the move history of the game.
         * Animates tokens on or off the board to transform the current
         * board into a snapshot of the desired move.
         */
        private function gotoMove(event:DynamicEvent):void{

            var move:ManantialesMove = ManantialesMove(event.move);

            // if move is before the currently selected move then iterate
            // back over the moves transforming the board
            // else iterate forward
            if(move.id < ManantialesMove(_moves[_selectedMoveInd]).id){
                do{
                    undoMove(ManantialesMove(_moves[_selectedMoveInd]));
                    _selectedMoveInd --;
                }while(move.id < ManantialesMove(_moves[_selectedMoveInd]).id
                        && _selectedMoveInd > 0);
            }else if (move.id > ManantialesMove(_moves[_selectedMoveInd]).id
                        && _selectedMoveInd < _moves.length){
                do{
                    doMove(ManantialesMove(_moves[_selectedMoveInd + 1]));
                    _selectedMoveInd ++;
                } while (move.id > ManantialesMove(_moves[_selectedMoveInd]).id
                    && _selectedMoveInd < _moves.length);
            }
        }

        public function dragDropCell(evt:DragEvent):void{

            if (evt.dragSource.hasFormat("token"))
            {
                // define cell and destination
                var token:ManantialesToken = ManantialesToken (evt.dragSource.dataForFormat("token"));
                var boardCell:BoardCell = BoardCell(evt.currentTarget);
                var destination:Ficha = Ficha(token.ficha);
                destination.row = boardCell.row;
                destination.column = boardCell.column;
                destination.type = token.ficha.type;

                var move:ManantialesMove = new ManantialesMove();
                move.currentCell = boardCell.token.cell;
                move.destinationCell = token.ficha;
                move.status = String (MoveStatus.UNVERIFIED);
                move.mode = _game.mode;
                move.id = 0;

                if (_currentPlayer.turn && token.cell.color == _currentPlayer.color) {
                    move.player = _currentPlayer;
                    var call:Object;
                    call = _gameService.doMove(_game, move);
                    call.operation = GAME_SERVICE_DO_MOVE_OP;
                    _executingMove = move;
                } else if (_currentPlayer.turn && token.cell.color != _currentPlayer.color) {
                    /* It can't be a move, so make a suggestion */
                    /* Find the move's player based upon destination cell color */
                    var players:ArrayCollection = _game.players;

                    for (var i:int = 0; i < players.length; i++) {
                        var player:ManantialesPlayer = ManantialesPlayer (players [ i ]);
                        if (player.color == token.cell.color) {
                            move.player = player;
                            break;
                        }
                    }

                    _suggestionHandler.makeSuggestion(move);
                }

                // do move in interface
                boardCell.reset();

                var newToken:ManantialesToken;

                if (token is ForestToken) {
                    _gameWindow.forestStore.removeToken();
                    newToken = new ForestToken();
                }
                else if (token is IntensiveToken) {
                    _gameWindow.intensiveStore.removeToken();
                    newToken = new IntensiveToken();
                }
                else if (token is ModerateToken) {
                    _gameWindow.moderateStore.removeToken();
                    newToken = new ModerateToken();
                }
                else if (token is ViveroToken) {
                        _gameWindow.viveroStore.removeToken();
                        newToken = new ViveroToken();
                }
                else if (token is SilvopastoralToken) {
                        _gameWindow.silvoStore.removeToken();
                        newToken = new SilvopastoralToken();
                }
                
                newToken.cell = destination;

                newToken.addEventListener(MouseEvent.MOUSE_DOWN, _suggestionHandler.startSuggestion);
                newToken.addEventListener(DragEvent.DRAG_COMPLETE, _suggestionHandler.makeSuggestion);                                                

                _gameWindow.board.addToken(newToken);
            }
        }

        public function dragExitCell(evt:DragEvent):void{

            // unselect board cell
            if (evt.dragSource.hasFormat("token")){
                var boardCell:BoardCell = BoardCell(evt.currentTarget);
                boardCell.reset();
            }
        }

        /*
         * Process the different types of messages received by the MessageReciever,
         * reordered and dispatched. All messages contain a game event
         * header, based on this different actions are taken.
         */
        private function processMessage(event:DynamicEvent):void {
            var message:IMessage = event.message;
            var gameEvent:String = message.headers.GAME_EVENT;
            var checkCondition:CheckCondition;
            var game:ManantialesGame;
            var move:ManantialesMove;
            var suggestion:Suggestion;

            switch (gameEvent) {
                case ManantialesEvent.BEGIN:
                    begin();
                    break;
                case ManantialesEvent.CHAT:
                    var chatMessage:ChatMessage = ChatMessage(message.body);
                    _gameWindow.chatPanel.addMessage(chatMessage);
                    if(chatMessage.sender.id != _currentPlayer.registrant.id){
                        _gameWindow.gameStatus.showMessage(
                            chatMessage.sender.registrant.name +
                            " has sent you a message", 0x000000);
                    }
                    break;
                case ManantialesEvent.END:
                    game = ManantialesGame(message.body);
                    handleGameEnd (game);
                    break;
                case ManantialesEvent.MOVE_COMPLETE:
                    move= ManantialesMove(message.body);
                    _gameWindow.playersViewer.updatePlayers();                   
                    addMove(move);
                    break;
                case ManantialesEvent.PLAYER_CHANGE:
                    game = ManantialesGame(message.body);
                    updatePlayers(game);
                    break;
                case ManantialesEvent.CONDITION_RAISED:
                    checkCondition = CheckCondition(message.body);
                    handleCheckConstraint (checkCondition);
                    break;
                case ManantialesEvent.CONDITION_RESOLVED:
                    checkCondition = CheckCondition(message.body);
                    handleCheckConstraintResolved (checkCondition);
                    break;
                case ManantialesEvent.CONDITION_TRIGGERED:
                    checkCondition = CheckCondition(message.body);
                    handleCheckConstraintTriggered (checkCondition);
                    break;
                case ManantialesEvent.STATE_CHANGE:
                    game = ManantialesGame(message.body);
                    handleStateChange (game);
                    break;
                case ManantialesEvent.SUGGESTION_EVALUATED:
                    suggestion = Suggestion(message.body);
                    _suggestionHandler.addSuggestion (suggestion);
                    break;
                case ManantialesEvent.SUGGESTION_APPLIED:
                    suggestion = Suggestion(message.body);
                    _suggestionHandler.removeSuggestion (suggestion);
                    break;

            }
        }

        /*
         * Game service result handler. Depending on the type of call
         * different actions are taken.
         */
        private function gameServiceResultHandler(event:ResultEvent):void{
            var call:Object = event.token;
            switch (call.operation){
                case GAME_SERVICE_GET_GRID_OP:
                    initGrid(GameGrid(event.result));
                    break;
                case GAME_SERVICE_GET_PLAYERS_OP:
                    updatePlayers(ManantialesGame (event.result));
                    break;
                case GAME_SERVICE_GET_MOVES_OP:
                    _moves = ArrayCollection(event.result)
                    _gameWindow.moveViewer.board = _gameWindow.board;
                    _gameWindow.moveViewer.initFromMoves(_moves);
                    _selectedMoveInd = _moves.length - 1;
                    break;
                case GAME_SERVICE_DO_MOVE_OP:
                    _executingMove = null;
                    break;
            }
        }

        /*
         * Called when game begins
         */
        private function begin():void{
            _gameWindow.gameStatus.showMessage("All players have joined. The game will now begin.",
                0x00000);
        }

        /*
         * Called when game ends
         */
        private function end():void{

                _gameWindow.gameStatus.showMessage("You have won the game!",
                   0x000000);

            if(_isEnded){
                return;
            }

            // remove the token store
            _gameWindow.forestStore.active = false;
            _gameWindow.moderateStore.active = false;
            _gameWindow.intensiveStore.active = false;
            if (_gameWindow.viveroStore != null)
                _gameWindow.viveroStore.active = false;
            if (_gameWindow.silvoStore != null)
                _gameWindow.silvoStore.active = false;
            _isEnded = true;
        }

        public function initGrid(gameGrid:GameGrid):void{
            _game.grid = gameGrid;
            _gameWindow.board.clearTokens();

            /* Setup undeveloped tokens first */
            for (var col:int = 0; col < this._gameWindow.board.nCols; col++) {
                for (var row:int = 0; row < this._gameWindow.board.nRows; row++) {
                    var roundCell:RoundCell = RoundCell(_gameWindow.board.getBoardCell(col, row));
                    if (roundCell != null) {
                        var tok:UndevelopedToken = new UndevelopedToken(col, row);
                        roundCell.token = tok;
                    }
                }
            }

            var ficha:Ficha;
            var token:ManantialesToken;

            /* reset token stores */
            if (_gameWindow.forestStore != null)
                _gameWindow.forestStore.reset();
            if (_gameWindow.moderateStore != null)
                _gameWindow.moderateStore.reset();
            if (_gameWindow.intensiveStore != null)
                _gameWindow.intensiveStore.reset();
            if (_gameWindow.viveroStore != null)
                _gameWindow.viveroStore.reset();
            if (_gameWindow.silvoStore != null)
                _gameWindow.silvoStore.reset();

            /* Now remove all placed tokens from corresonding store */
            if (_game.grid.cells && _game.grid.cells.length > 0) {
                for (var i:Number = 0; i < _game.grid.cells.length; i++){
                    ficha = Ficha(_game.grid.cells[i]);
                    switch (ficha.type) {
                        case TokenType.FOREST:
                           token = new ForestToken();
                           token.ficha = ficha;
                           if (this._gameWindow.forestStore && token.ficha.color == this._currentPlayer.color)
                               this._gameWindow.forestStore.removeToken();
                           break;
                        case TokenType.MODERATE:
                           token = new ModerateToken();
                           token.ficha = ficha;
                           if (this._gameWindow.moderateStore && token.ficha.color == this._currentPlayer.color)
                               this._gameWindow.moderateStore.removeToken();
                           break;
                        case TokenType.INTENSIVE:
                           token = new IntensiveToken();
                           token.ficha = ficha;
                           if (this._gameWindow.intensiveStore && token.ficha.color == this._currentPlayer.color)
                                this._gameWindow.intensiveStore.removeToken();
                           break;
                        case TokenType.VIVERO:
                           token = new ViveroToken();
                           token.ficha = ficha;
                           if (this._gameWindow.viveroStore && token.ficha.color == this._currentPlayer.color)
                                this._gameWindow.viveroStore.removeToken();
                           break;
                        case TokenType.SILVOPASTORAL:
                           token = new SilvopastoralToken();
                           token.ficha = ficha;
                           if (this._gameWindow.silvoStore && token.ficha.color == this._currentPlayer.color)
                                this._gameWindow.silvoStore.removeToken();
                           break;
                        default:
                            break;
                    }

                    _gameWindow.board.addToken(token);

                    if (_game.mode == "BASIC_PUZZLE" || _game.mode == "SILVO_PUZZLE")
                        token.addEventListener(MouseEvent.MOUSE_DOWN, _suggestionHandler.startSuggestion);
                }
            }
        }

        private function initTurn():void{
            if (_game.mode != null)  {
               _gameWindow.currentState= _game.mode;
            }

            // setup token store panels for hiding
            _tokenStorePanels.addItem(_gameWindow.mfp);
            _tokenStorePanels.addItem(_gameWindow.mgp);
            _tokenStorePanels.addItem(_gameWindow.igp);
            if (_gameWindow.currentState == "SILVOPASTORAL") {
                _tokenStorePanels.addItem(_gameWindow.sep);
                _tokenStorePanels.addItem(_gameWindow.sap);
            }

            // initialize token stores
            for (var i:int = 0; i < _tokenStorePanels.length; i++) {
                initializeTokenStorePanel (Panel(_tokenStorePanels.getItemAt(i)));
            }

            // open annual conditions generator
            if ( (_game.mode == Mode.CLASSIC || _game.mode == Mode.SILVOPASTORAL) &&_annCondGen == null) {
                _annCondGen = new AnnualConditionsGenerator();
                _annCondGen.addEventListener("result", handleAnnCondResult);
                PopUpManager.addPopUp(_annCondGen, _gameWindow, true);
                PopUpManager.centerPopUp(_annCondGen);
            }
        }

        private function handleAnnCondResult(event:DynamicEvent):void{
            PopUpManager.removePopUp(_annCondGen);
            _annCondGen = null;
            if(!event.isGoodYear){
                var move:ManantialesMove = new ManantialesMove ();
                move.badYear = true;
                move.player = _currentPlayer;
                move.mode = _game.mode;
                var call:Object = _gameService.doMove(_game, move);
                call.operation = GAME_SERVICE_DO_MOVE_OP;
            }
        }

        private function handleCheckResult(event:DynamicEvent):void {
            var idx:int = -1;

            for (var i:int = 0; i < _alerts.length; i++) {
                var condition:CheckConstraintAlert = CheckConstraintAlert(_alerts.getItemAt(i));
                if (condition.acknowledged) {
                        idx = i;
                        break;
                }
            }

            if (idx > -1) {
            var alert:CheckConstraintAlert = CheckConstraintAlert (_alerts.removeItemAt(idx));
               PopUpManager.removePopUp(alert);
            }
        }

        private function handleStateChangeResult (event:DynamicEvent):void {
           PopUpManager.removePopUp(_stageChangeAlert);
           _stageChangeAlert = null;

           /* Reinitialize the grid in case a move was picked from the previous
           state change */
            var callGrid:Object = _gameService.getGameGrid(_gameId);
            callGrid.operation = GAME_SERVICE_GET_GRID_OP;

            var callPlayers:Object = _gameService.getPlayers(_gameId);
            callPlayers.operation = GAME_SERVICE_GET_PLAYERS_OP;

            var callMoves:Object = _gameService.getMoves(_gameId);
            callMoves.operation = GAME_SERVICE_GET_MOVES_OP;
        }

        private function handleEndResult (event:DynamicEvent):void {
                PopUpManager.removePopUp(this._endAlert);
                _endAlert = null;
                this._gameWindow.dispatchCompletion();
        }

        private function endTurn():void{
                   /* return the window to the default state */
                _gameWindow.currentState = "";
        }

        public function set isTurn(isTurn:Boolean):void{
            if(_isTurn != isTurn){
                _isTurn = isTurn;
                if (_isTurn){
                    initTurn();
                }else{
                    endTurn();
                }
            }
        }

        /**
         * Updates the list of players. Also updates the chat panel and
         * players viewer components.
         *
         * @param players the new list of players
         */
        public function updatePlayers(game:ManantialesGame):void{
            this._game = game;

            var gamePlayer:ManantialesPlayer;
            var i:int;

            for (i = 0; i < game.players.length; i++){
                gamePlayer = ManantialesPlayer(game.players[i]);
                if (gamePlayer.id == _currentPlayer.id){
                    _currentPlayer = gamePlayer;
                    _gameWindow.chatPanel.currentPlayer = _currentPlayer;
                    this.isTurn = _currentPlayer.turn;
                }
                if (gamePlayer.turn){
                    if (gamePlayer.id == _currentPlayer.id){
                        _gameWindow.gameStatus.showMessage("Its your turn",
                            Color.getColorCode(_currentPlayer.color));
                        _gameWindow.currentState = _game.mode;
                    }else{
                        _gameWindow.gameStatus.showMessage(
                            gamePlayer.registrant.name + " to move", Color.getColorCode(gamePlayer.color));
                    }
                }
            }

            _gameWindow.playersViewer.players = _game.players;
            _gameWindow.currentGame = game;
            _players = _game.players;

            _gameWindow.stateMenuBar.dataProvider = _gameWindow.stateMenu;
            _gameWindow.stateMenuBar.executeBindings();

        }

        public function quitGame (gamePlayer:GamePlayer):void {
            var call:Object = _gameService.quitGame(_game, gamePlayer);
            call.operation = "quitGame";
        }

        private function addMove(move:ManantialesMove):void{

            if (move.mode == _game.mode && !_isTurn)
                _gameWindow.currentState = "";

            if (move.mode == _game.mode) {

                //get last move in game
                var lastMove:ManantialesMove = null;
                if (_moves.length > 0){
                    lastMove = ManantialesMove(_moves[length - 1]);
                }

                //if move is after the last move then add moves
                //else update the move since its info may have changed
                if (lastMove == null || move.id > lastMove.id){

                    //add to moves
                    _moves.source.push(move);
                    _gameWindow.moveViewer.addMove(move);

                    //if current move is the last move then animate
                    if (_selectedMoveInd == _moves.length - 2){
                        _selectedMoveInd ++;
                        doMove(move);
                    }
                } else {

                    // Search for move in reverse order because its most likely to be the last move
                    var oldMove:ManantialesMove;
                    for (var i:Number = _moves.length - 1; i >= 0; i--){
                        oldMove = ManantialesMove(_moves[i]);
                        if (oldMove.id == move.id){
                            _moves[i] = move;
                            _gameWindow.moveViewer.updateMove(move);
                            break;
                        }
                    }
                }


                /* Finally, if game is in puzzle mode, add suggestion handlers to move */
                if (_game.mode == Mode.BASIC_PUZZLE || _game.mode == Mode.SILVO_PUZZLE) {
                    /* find the token associated with the move */
                    var suggestable:RoundCell = RoundCell(_gameWindow.board.getBoardCell(
                        move.destinationCell.column, move.destinationCell.row));
                    if (suggestable != null) {
                        var ficha:Ficha = Ficha (move.destinationCell);
                             //create new token
                        var token:ManantialesToken;

                        switch (ficha.type) {
                            case TokenType.FOREST:
                               token = new ForestToken();
                               break;
                            case TokenType.INTENSIVE:
                               token = new IntensiveToken();
                               break;
                            case TokenType.MODERATE:
                               token = new ModerateToken();
                               break;
                            case TokenType.SILVOPASTORAL:
                               token = new SilvopastoralToken();
                               break;
                            case TokenType.VIVERO:
                               token = new ViveroToken();
                               break;
                            default:
                               Alert.show("Unable to determine type of ficha:" + ficha.toString());
                               break;
                        }

                        token.cell = move.destinationCell;
                        token.ficha = ficha;
                        token.mouseEnabled = true;                        
                        token.addEventListener(MouseEvent.MOUSE_DOWN, _suggestionHandler.startSuggestion);
                        suggestable.token = token;
                    }
                }
            }
        }

        /*
         * Handles faults from game service calls
         */
        private function gameServiceFaultHandler(event:FaultEvent):void{
            var errorMessage:ErrorMessage = ErrorMessage(event.message);
            if (errorMessage.extendedData != null){
                if(errorMessage.extendedData[ExceptionType.EXCEPTION_TYPE_KEY] == ExceptionType.INVALID_MOVE){
                    var fnc:Function = function (event:CloseEvent):void{
                        undoMove(_executingMove);
                    }
                    Alert.show("Sorry but this move is not valid", "Woops!", Alert.OK, null, fnc);
                }
            }else{
                Alert.show(event.fault.faultString, "Server Error");
            }
        }

        /*
         * Animates a move
         */
        private function doMove(move:ManantialesMove):void{

            // if was a bad year then nothing to do
            if(move.badYear){
                _gameWindow.moveViewer.selectedMove = ManantialesMove(_moves[_selectedMoveInd + 1]);
                return
            }

            //check that destination is free
            var boardCell:RoundCell = RoundCell(_gameWindow.board.getBoardCell(
                move.destinationCell.column, move.destinationCell.row));
            if (!boardCell.token is UndevelopedToken ||
                boardCell.token is IntensiveToken)
            {
                _gameWindow.moveViewer.selectedMove = move;
                return;
            }

            var current:Ficha;
            if (move.currentCell != null && move.currentCell is Ficha)
                current = Ficha (move.currentCell);
            var destination:Ficha = Ficha(move.destinationCell);

            //define origin
            var startPoint:Point;
            var startSize:Number;

            if(move.player.id == _currentPlayer.id && _isTurn){
                switch (destination.type) {
                        case TokenType.FOREST:
                           startPoint = new Point(_gameWindow.forestStore.width, _gameWindow.forestStore.height);
                       startPoint = _gameWindow.forestStore.localToGlobal(startPoint);
                       break;
                    case TokenType.INTENSIVE:
                        startPoint = new Point(_gameWindow.intensiveStore.width, _gameWindow.intensiveStore.height);
                        startPoint = _gameWindow.intensiveStore.localToGlobal(startPoint);
                            break;
                        case TokenType.MODERATE:
                        startPoint = new Point(_gameWindow.moderateStore.width, _gameWindow.moderateStore.height);
                        startPoint = _gameWindow.moderateStore.localToGlobal(startPoint);
                        break;
                    case TokenType.SILVOPASTORAL:
                        startPoint = new Point(_gameWindow.silvoStore.width, _gameWindow.silvoStore.height);
                        startPoint = _gameWindow.silvoStore.localToGlobal(startPoint);
                        break;
                    case TokenType.VIVERO:
                        startPoint = new Point(_gameWindow.viveroStore.width, _gameWindow.viveroStore.height);
                        startPoint = _gameWindow.viveroStore.localToGlobal(startPoint);
                        break;
                   default:
                        break;
                }

                startPoint = _gameWindow.animateLayer.globalToLocal(startPoint);
                startSize = _gameWindow.board.tokenSize;

            } else {
                var playerBtn:Button = _gameWindow.playersViewer.getPlayerButton(
                    ManantialesPlayer(move.player));
                startPoint = new Point(
                    playerBtn.x + Color.getCellIconSize() / 2 + 5,
                    playerBtn.y + Color.getCellIconSize() / 2 + 5);
                startPoint = _gameWindow.playersViewer.localToGlobal(startPoint);
                startPoint = _gameWindow.animateLayer.globalToLocal(startPoint);
                startSize = Color.getCellIconSize();
            }

            //define destination
            var endPoint:Point = new Point(boardCell.width / 2, boardCell.height / 2);
            var endSize:Number = _gameWindow.board.tokenSize;
            endPoint = boardCell.localToGlobal(endPoint);
            endPoint = _gameWindow.animateLayer.globalToLocal(endPoint);

            //create new token
            var token:ManantialesToken;

            switch (destination.type) {
                case TokenType.FOREST:
                   token = new ForestToken();
                   break;
                case TokenType.INTENSIVE:
                   token = new IntensiveToken();
                   break;
                case TokenType.MODERATE:
                   token = new ModerateToken();
                   break;
                case TokenType.SILVOPASTORAL:
                   token = new SilvopastoralToken();
                   break;
                case TokenType.VIVERO:
                   token = new ViveroToken();
                   break;
                default:
                   break;
            }

            token.cell = move.destinationCell;
            token.width = endSize;
            token.height = endSize;
            _gameWindow.animateLayer.addChild(token);

            if (_game.mode == "BASIC_PUZZLE" || _game.mode == "SILVO_PUZZLE") {
                token.addEventListener(MouseEvent.MOUSE_DOWN, _suggestionHandler.startSuggestion);
            }

            //define motion animation
            var apX:AnimateProperty = new AnimateProperty(token);
            apX.fromValue = startPoint.x;
            apX.toValue = endPoint.x;
            apX.duration = 1000;
            apX.property = "x";
            var apY:AnimateProperty = new AnimateProperty(token);
            apY.fromValue = startPoint.y;
            apY.toValue = endPoint.y;
            apY.duration = 1000;
            apY.property = "y";
            apY.addEventListener(EffectEvent.EFFECT_END, endDoMove);

            //define size animation
            var apXScale:AnimateProperty = new AnimateProperty(token);
            apXScale.property = "scaleX";
            apXScale.fromValue = startSize / endSize;
            apXScale.toValue = 1;
            apXScale.duration = 1000;
            var apYScale:AnimateProperty = new AnimateProperty(token);
            apYScale.property = "scaleY";
            apYScale.fromValue = startSize / endSize;
            apYScale.toValue = 1;
            apYScale.duration = 1000;

            //start effect
            apX.play();
            apY.play();
            apXScale.play();
            apYScale.play();
        }

        private function endDoMove(event:EffectEvent):void{

            var token:ManantialesToken = ManantialesToken(AnimateProperty(event.currentTarget).target);
            var boardCell:BoardCell = _gameWindow.board.getBoardCell(token.cell.column, token.cell.row);
            _gameWindow.animateLayer.removeChild(token);

            //remove from token store if necessary
            if(token.cell.color == _currentPlayer.color && _isTurn){
                if (token is ForestToken) {
                    _gameWindow.forestStore.removeToken();
                }
                else if (token is IntensiveToken) {
                    _gameWindow.intensiveStore.removeToken();
                }
                else if (token is ModerateToken) {
                    _gameWindow.moderateStore.removeToken();
                }
                else if (token is ViveroToken) {
                    _gameWindow.viveroStore.removeToken();
                }
                else if (token is SilvopastoralToken) {
                    _gameWindow.silvoStore.removeToken();
                }
            }

            boardCell.token = token;
            boardCell.token.blink(1);

            // Update move viewer
            if (_moves [_selectedMoveInd] != null && _selectedMoveInd > 0) {
                var move:ManantialesMove = ManantialesMove(_moves[_selectedMoveInd])
                _gameWindow.moveViewer.selectedMove = move;
            }
        }

        private function undoMove(move:ManantialesMove):void{

            // if was a bad year then nothing to undo
            if(move.badYear){
                _gameWindow.moveViewer.selectedMove = ManantialesMove(_moves[_selectedMoveInd - 1]);
                return
            }

            //define origin
            var boardCell:BoardCell = _gameWindow.board.getBoardCell(move.destinationCell.column,
                    move.destinationCell.row);
            var startPoint:Point = new Point(boardCell.width / 2, boardCell.height / 2);
            var startSize:Number = _gameWindow.board.tokenSize;
            startPoint = boardCell.localToGlobal(startPoint);
            startPoint = _gameWindow.animateLayer.globalToLocal(startPoint);

            //define destination
            var playerBtn:Button = _gameWindow.playersViewer.getPlayerButton(ManantialesPlayer(move.player));
            var endPoint:Point = new Point(playerBtn.x + Color.getCellIconSize() / 2 + 5, playerBtn.y +
                    Color.getCellIconSize() / 2 + 5);
            endPoint = _gameWindow.playersViewer.localToGlobal(endPoint);
            endPoint = _gameWindow.animateLayer.globalToLocal(endPoint);
            var endSize:Number = Color.getCellIconSize();

            //create new token
            var token:ManantialesToken = new ManantialesToken();
            token.cell = move.destinationCell;
            token.width = endSize;
            token.height = endSize;
            _gameWindow.animateLayer.addChild(token);

            // restore previous token
            for (var i:int = 0; i < _moves.length; i++) {
                var possible:ManantialesMove = _moves [ _moves.length - i];
                if (possible.destinationCell == move.currentCell)
                    var ficha:Ficha = Ficha (possible.destinationCell);
                    switch (ficha.type) {
                        case TokenType.INTENSIVE:
                            boardCell.token = new IntensiveToken();
                            break;
                        case TokenType.MODERATE:
                            boardCell.token = new ModerateToken();
                            break;
                        case TokenType.FOREST:
                            boardCell.token = new ForestToken();
                            break;
                        case TokenType.VIVERO:
                            boardCell.token = new ViveroToken();
                            break;
                        case TokenType.SILVOPASTORAL:
                            boardCell.token = new SilvopastoralToken();
                            break;
                        default:
                            boardCell.token = new UndevelopedToken(ficha.column, ficha.row);

                    }
            }

            boardCell.reset();

            //define motion animation
            var apX:AnimateProperty = new AnimateProperty(token);
            apX.fromValue = startPoint.x;
            apX.toValue = endPoint.x;
            apX.duration = 1000;
            apX.property = "x";
            var apY:AnimateProperty = new AnimateProperty(token);
            apY.fromValue = startPoint.y;
            apY.toValue = endPoint.y;
            apY.duration = 1000;
            apY.property = "y";
            apY.addEventListener(EffectEvent.EFFECT_END, endUndoMove);

            //define size animation
            var apXScale:AnimateProperty = new AnimateProperty(token);
            apXScale.property = "scaleX";
            apXScale.fromValue = startSize / endSize;
            apXScale.toValue = 1;
            apXScale.duration = 1000;
            var apYScale:AnimateProperty = new AnimateProperty(token);
            apYScale.property = "scaleY";
            apYScale.fromValue = startSize / endSize;
            apYScale.toValue = 1;
            apYScale.duration = 1000;

            //start effect
            apX.play();
            apY.play();
            apXScale.play();
            apYScale.play();
        }

        private function endUndoMove(event:EffectEvent):void {

            var token:ManantialesToken = ManantialesToken(AnimateProperty(event.currentTarget).target);
            _gameWindow.animateLayer.removeChild(token);

            //add to token store if necessary
            if(_isTurn) {
                var ficha:Ficha = Ficha(token.cell);
                switch (ficha.type) {
                        case TokenType.FOREST:
                          _gameWindow.forestStore.addToken();
                          break;
                        case TokenType.INTENSIVE:
                          _gameWindow.intensiveStore.addToken();
                          break;
                        case TokenType.MODERATE:
                          _gameWindow.moderateStore.addToken();
                          break;
                        case TokenType.SILVOPASTORAL:
                          _gameWindow.silvoStore.addToken();
                          break;
                        case TokenType.VIVERO:
                          _gameWindow.viveroStore.addToken();
                          break;
                        default:
                          break;
                }
            }

            if (_selectedMoveInd >= 0)
                _gameWindow.moveViewer.selectedMove = ManantialesMove(_moves[_selectedMoveInd]);
        }

        private function handleCheckConstraint (checkCondition:CheckCondition):void {
            var checkConAlert:CheckConstraintAlert = new CheckConstraintAlert();
            checkConAlert.constraint = checkCondition;
            checkConAlert.raised = true;
            checkConAlert.addEventListener("result", handleCheckResult);
            _alerts.addItem(checkConAlert);
            PopUpManager.addPopUp(checkConAlert, _gameWindow, true);
            PopUpManager.centerPopUp(checkConAlert);
        }

        private function handleCheckConstraintResolved (checkCondition:CheckCondition):void {
            var checkConAlert:CheckConstraintAlert = new CheckConstraintAlert();
            checkConAlert.constraint = checkCondition;
            checkConAlert.raised = false;
            checkConAlert.addEventListener("result", handleCheckResult);
            _alerts.addItem(checkConAlert);
            PopUpManager.addPopUp(checkConAlert, _gameWindow, true);
            PopUpManager.centerPopUp(checkConAlert);
        }

        private function handleCheckConstraintTriggered (checkCondition:CheckCondition):void {
            var checkConAlert:CheckConstraintAlert = new CheckConstraintAlert();
            checkConAlert.constraint = checkCondition;
            checkConAlert.raised = false;
            checkConAlert.triggered = true;
            checkConAlert.addEventListener("result", handleCheckResult);
            _alerts.addItem(checkConAlert);
            PopUpManager.addPopUp(checkConAlert, _gameWindow, true);
            PopUpManager.centerPopUp(checkConAlert);

             /* Reset the grid on the board */
            var callGrid:Object = _gameService.getGameGrid(_gameId);
            callGrid.operation = GAME_SERVICE_GET_GRID_OP;
        }

        private function handleStateChange (game:ManantialesGame):void {
            if (_stageChangeAlert == null) {
                _stageChangeAlert = new GraphicAlert();
                if (game == null) {
                    Alert.show("Null game passed in.");
                    game = _game;
                }
                _stageChangeAlert.text = "Stage complete. Progressing to next stage, '" +
                      game.mode + "'";
                _stageChangeAlert.addEventListener ("result", handleStateChangeResult);
                PopUpManager.addPopUp(_stageChangeAlert, _gameWindow, true);
                PopUpManager.centerPopUp(_stageChangeAlert);

                this._game = game;
                this._moves = new ArrayCollection();
                this._gameWindow.currentState =  _game.mode;
            }
        }

        private function handleGameEnd (game:ManantialesGame):void {
                var i:int;

                if (_endAlert == null) {
                        _endAlert = new GraphicAlert();

                        var expiredCondition:CheckCondition = null;

                        /* See if any checkConstraints are expired and if so, Post a
                        different end condition alert */
                        for (i = 0; i < game.checkConditions.length; i++) {
                                var checkCondition:CheckCondition = CheckCondition(
                                     game.checkConditions.getItemAt(i));
                                if (checkCondition.expired && (checkCondition.reason == ConditionType.MANANTIALES_DRY ||
                                     checkCondition.reason == ConditionType.TERRITORY_DEFORESTED))
                            {
                                expiredCondition = checkCondition;
                                }
                        }

                        if (expiredCondition != null) {
                          _endAlert.text = "Game Over due to expiry of SEVERE CheckCondition ('" +
                                   expiredCondition.reason + "')";
                          _endAlert.positive = false;
                        } else {
                            if (_isTurn){
                                _endAlert.text = "Game Over.  You have won!";
                                _endAlert.positive = true;
                                end();
                            } else {
                                _endAlert.text= "Game Over.  You have lost.";
                                _endAlert.positive = false;
                            }
                   }

                    _endAlert.addEventListener("result",handleEndResult);

                    /* Remove all alert conditions from the PopUpManager */
                for (i = 0; i < _alerts.length; i++) {
                        PopUpManager.removePopUp(IFlexDisplayObject(_alerts.getItemAt(i)));
                }

                    PopUpManager.addPopUp(_endAlert, _gameWindow, true);
                    PopUpManager.centerPopUp(_endAlert);
                }
        }


        public function destroy():void{
            _msgReceiver.destroy();
        }

        }
    }