//copyright

package mx.ecosur.multigame.manantiales
{

    import flash.events.MouseEvent;
    import flash.geom.Point;
    
    import mx.collections.ArrayCollection;
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
    import mx.ecosur.multigame.manantiales.entity.SimpleAgent;
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
        
        private var _annCondGen:AnnualConditionsGenerator;
        private var _alerts:ArrayCollection;
        private var _endAlert:GraphicAlert;
        private var _stageChangeAlert:GraphicAlert;
        private var _suggestionHandler:SuggestionHandler;
        private var _tokenHandler:TokenHandler;

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

        /* Needed to force compilation of SimplePlayer in .swf file */
        private var _unUsed:SimpleAgent;
        

        public function ManantialesGameController (gameWindow:ManantialesWindow)
        {

            // set references
            _gameWindow = gameWindow;
            _currentPlayer = ManantialesPlayer (gameWindow.currentPlayer);
            _game = ManantialesGame(gameWindow.currentGame);
            _gameId = _game.id;

                // instantiate collections
            _moves = new ArrayCollection();
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

            /* Setup managers (handlers) */
            _suggestionHandler = new SuggestionHandler (this);
            _tokenHandler = new TokenHandler (_gameWindow, _currentPlayer, _suggestionHandler)

            // initialize game status
            _gameWindow.gameStatus.showMessage("Welcome to the game " +
                _currentPlayer.registrant.name + "!\n\n You are " +
                Color.getColorDescription(_currentPlayer.color),
                Color.getColorCode(_currentPlayer.color));

                        // initialize the board
            _gameWindow.board.dragEnterHandler = dragEnterBoardCell;
            _gameWindow.board.dragDropHandler = _tokenHandler.dragDropCell;
            _gameWindow.board.dragExitHandler = _tokenHandler.dragExitCell;

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
        }

        public function dragEnterBoardCell(evt:DragEvent):void{

            if (evt.dragSource.hasFormat("token")){
                var token:ManantialesToken = ManantialesToken(evt.dragSource.dataForFormat("token"));
                var boardCell:RoundCell = RoundCell(evt.currentTarget);
                boardCell.addEventListener(DragEvent.DRAG_EXIT, _tokenHandler.dragExitCell);

                _previousToken = boardCell.token;

                // calculate if move is valid
                if (validateMove(boardCell, token)){
                    boardCell.select(token.cell.colorCode);
                    DragManager.acceptDragDrop(boardCell);
                }
            }
        }

        private function validateMove(boardCell:RoundCell, token:ManantialesToken):Boolean {
            var ret:Boolean = false;
            if (_game.mode != Mode.BASIC_PUZZLE && _game.mode != Mode.SILVO_PUZZLE) {
                if (boardCell.color == token.cell.color) {
                        ret = true;
                } else if (boardCell.color == Color.UNKNOWN) {
                    switch (token.cell.color) {
                        case Color.YELLOW:
                            ret = (boardCell.column < 5 && boardCell.row < 5);
                            break;
                        case Color.PURPLE:
                            ret = (boardCell.column < 5 && boardCell.row > 3);
                            break;
                        case Color.RED:
                            ret = (boardCell.column > 3 && boardCell.row < 5);
                            break;
                        case Color.BLACK:
                            ret = (boardCell.column > 3 && boardCell.row > 3);
                            break;
                   }
                }
            } else {
                if (boardCell.token.cell == null) {
                    switch (token.cell.color) {
                        case Color.YELLOW:
                            ret = (boardCell.column < 5 && boardCell.row < 5);
                            break;
                        case Color.PURPLE:
                            ret = (boardCell.column < 5 && boardCell.row > 3);
                            break;
                        case Color.RED:
                            ret = (boardCell.column > 3 && boardCell.row < 5);
                            break;
                        case Color.BLACK:
                            ret = (boardCell.column > 3 && boardCell.row > 3);
                            break;
                   }
                }
            }

            return ret;
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

        
        public function get puzzleMode():Boolean {
            return (_game.mode == "BASIC_PUZZLE" || _game.mode == "SILVO_PUZZLE");
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

        public function initGrid(gameGrid:GameGrid):void {
            _game.grid = gameGrid;
            _gameWindow.board.clearTokens();

            /* Setup undeveloped tokens first */
            for (var col:int = 0; col < this._gameWindow.board.nCols; col++) {
                for (var row:int = 0; row < this._gameWindow.board.nRows; row++) {
                    var roundCell:RoundCell = RoundCell(_gameWindow.board.getBoardCell(col, row));
                    if (roundCell != null) {
                        var tok:UndevelopedToken = new UndevelopedToken();
                        roundCell.token = tok;
                    }
                }
            }

            var ficha:Ficha;
            var token:ManantialesToken;

            /* TODO: Move to store objects themselves. */
            if (_game.grid.cells && _game.grid.cells.length > 0) {
                for (var i:int = 0; i < _game.grid.cells.length; i++){
                    ficha = Ficha(_game.grid.cells[i]);
                    switch (ficha.type) {
                        case TokenType.FOREST:
                           token = new ForestToken();
                           break;
                        case TokenType.MODERATE:
                           token = new ModerateToken();
                           break;
                        case TokenType.INTENSIVE:
                           token = new IntensiveToken();
                           break;
                        case TokenType.VIVERO:
                           token = new ViveroToken();
                           break;
                        case TokenType.SILVOPASTORAL:
                           token = new SilvopastoralToken();
                           break;
                        default:
                            break;
                    }
                    token.ficha = ficha;
                    _gameWindow.board.addToken(token);

                    if (puzzleMode)
                        _tokenHandler.addListeners (token);
                }
            }
        }

        private function initTurn():void {
            if (_game.mode != null)  {
               _gameWindow.currentState= _game.mode;
            }

            _tokenHandler.initializeTokenStores();            

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
            _gameWindow.playersViewer.game = _game;

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
                    }else{
                        _gameWindow.gameStatus.showMessage(
                            gamePlayer.registrant.name + " to move", Color.getColorCode(gamePlayer.color));
                    }
                }
            }

            _gameWindow.currentGame = game;
            _players = _game.players;

            _gameWindow.stateMenuBar.dataProvider = _gameWindow.stateMenu;
            _gameWindow.stateMenuBar.executeBindings();
            _tokenHandler.update(_currentPlayer);
        }
        
        public function quitGame (gamePlayer:GamePlayer):void {
            var call:Object = _gameService.quitGame(_game, gamePlayer);
            call.operation = "quitGame";
        }

        private function addMove(move:ManantialesMove):void {

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
                    
                    /* Suggestions are cleared on completion of any turn based moves */
                    if (move.player.turn)
                        _suggestionHandler.clearSuggestions();

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
                
                /* If PUZZLE, check and see if the move was suggested, and if so,
                remove the token connected to the move's "currentCell" -- thereby
                completing the suggested move */
                if (puzzleMode && move.currentCell != null) {
                    var cell:RoundCell = RoundCell(_gameWindow.board.getBoardCell(
                        move.currentCell.column, move.currentCell.row));
                    /* Set the previous to Untouched Forest */
                    cell.token = new UndevelopedToken();
                    cell.reset();
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
                    Alert.show("Invalid Move", "Woops!", Alert.OK, null, fnc);
                }
            }else{
                Alert.show(event.fault.faultString, "Server Error");
            }
        }

        public function sendMove (move:ManantialesMove):void {
            var call:Object = _gameService.doMove(_game, move);
            call.operation = "doMove";
            _executingMove = move;
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
            
            var boardCell:RoundCell;

            //check that destination is free
            if (move.destinationCell != null) {
               boardCell = RoundCell(_gameWindow.board.getBoardCell(
                    move.destinationCell.column, move.destinationCell.row));
                if (!boardCell.token is UndevelopedToken ||
                    boardCell.token is IntensiveToken)
                {
                    _gameWindow.moveViewer.selectedMove = move;
                    return;
                }
            } else {
                boardCell = RoundCell(_gameWindow.board.getBoardCell(
                    move.currentCell.column, move.currentCell.row));
                var token:ManantialesToken = new UndevelopedToken();
                token.cell = move.currentCell;
                boardCell.reset();
            }

            var current:Ficha;
            var currentCell:RoundCell;
            if (move.currentCell != null && move.currentCell is Ficha){
                current = Ficha (move.currentCell);
                currentCell = RoundCell(_gameWindow.board.getBoardCell(current.column, current.row));
            }
            var destination:Ficha 
            if (move.destinationCell != null) 
                destination = Ficha(move.destinationCell);

            //define origin
            var startPoint:Point;
            var startSize:Number;
            var playerBtn:Button;

            if(move.player.id == _currentPlayer.id && _isTurn){
                if (currentCell == null) {
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
                } else {
                    startPoint = new Point(current.width, current.height);
                    startPoint = currentCell.localToGlobal(startPoint);
                }

                startPoint = _gameWindow.animateLayer.globalToLocal(startPoint);
                startSize = _gameWindow.board.tokenSize;

            } else if (currentCell != null) {
                startPoint = new Point(currentCell.width / 2, currentCell.height / 2);
                startSize = _gameWindow.board.tokenSize;
                startPoint = currentCell.localToGlobal(startPoint);
                startPoint = _gameWindow.animateLayer.globalToLocal(startPoint);
            } else {
                playerBtn = _gameWindow.playersViewer.getPlayerButton(
                    ManantialesPlayer(move.player));
                startPoint = new Point(
                    playerBtn.x + Color.getCellIconSize() / 2 + 5,
                    playerBtn.y + Color.getCellIconSize() / 2 + 5);
                startPoint = _gameWindow.playersViewer.localToGlobal(startPoint);
                startPoint = _gameWindow.animateLayer.globalToLocal(startPoint);
                startSize = Color.getCellIconSize();
            }
            
            var endPoint:Point;
            var endSize:Number;

            //define destination
            if (destination != null) {
                endPoint = new Point(boardCell.width / 2, boardCell.height / 2);
                endSize = _gameWindow.board.tokenSize;
                endPoint = boardCell.localToGlobal(endPoint);
                endPoint = _gameWindow.animateLayer.globalToLocal(endPoint);
            } else if (move.currentCell != null) {
                playerBtn = _gameWindow.playersViewer.getPlayerButton(
                    ManantialesPlayer(move.player));
                endPoint = new Point(
                    playerBtn.x + Color.getCellIconSize() / 2 + 5,
                    playerBtn.y + Color.getCellIconSize() / 2 + 5);
                endPoint = _gameWindow.playersViewer.localToGlobal(startPoint);
                endPoint = _gameWindow.animateLayer.globalToLocal(startPoint);
                endSize = Color.getCellIconSize();
            }

            //create new token
            var token:ManantialesToken;
            
            if (destination != null) {
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
            } else if (move.currentCell != null) {
                token = new UndevelopedToken();
            }
                
            token.width = endSize;
            token.height = endSize;
            _gameWindow.animateLayer.addChild(token);

            if(puzzleMode){
                _tokenHandler.addListeners(token);
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
            if (token.cell != null) {
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
                if (_selectedMoveInd > 0 && _moves.length > _selectedMoveInd && _moves[_selectedMoveInd] != null) {
                    var move:ManantialesMove = ManantialesMove(_moves[_selectedMoveInd])
                    _gameWindow.moveViewer.selectedMove = move;
                }
            }
        }

        private function undoMove(move:ManantialesMove):void{
            var boardCell:BoardCell;
            var startPoint:Point;
            var startSize:Number
            
            var endCell:BoardCell;
            var endPoint:Point;
            var endSize:Number;
            var playerBtn:Button

            // if was a bad year then nothing to undo
            if(move.badYear){
                _gameWindow.moveViewer.selectedMove = ManantialesMove(_moves[_selectedMoveInd - 1]);
                return
            }
            

            //define origin
            boardCell = _gameWindow.board.getBoardCell(move.destinationCell.column,
                    move.destinationCell.row);
            startPoint = new Point(boardCell.width / 2, boardCell.height / 2);
            startSize = _gameWindow.board.tokenSize;
            startPoint = boardCell.localToGlobal(startPoint);
            startPoint = _gameWindow.animateLayer.globalToLocal(startPoint);

            //define destination
            if (move.currentCell == null) {
                playerBtn = _gameWindow.playersViewer.getPlayerButton(ManantialesPlayer(move.player));
                endPoint = new Point(playerBtn.x + Color.getCellIconSize() / 2 + 5, playerBtn.y +
                    Color.getCellIconSize() / 2 + 5);
                endPoint = _gameWindow.playersViewer.localToGlobal(endPoint);
                endPoint = _gameWindow.animateLayer.globalToLocal(endPoint);
                endSize = Color.getCellIconSize();
            } else {
                
                endCell = _gameWindow.board.getBoardCell(move.currentCell.column, move.currentCell.row);
                endPoint = new Point (endCell.width/ 2, endCell.height/2);
                endSize = _gameWindow.board.tokenSize;
                endPoint = endCell.localToGlobal(endPoint);
                endPoint = _gameWindow.animateLayer.globalToLocal(endPoint);
                
            }

            // restore previous token, if determinable from move 
            if (move.currentCell != null) {
                for (var index:int = 1; index <= _moves.length; index++) {
                    var possible:ManantialesMove = _moves [ _moves.length - index];
                    if (possible.destinationCell == move.currentCell) {
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
                                boardCell.token = new UndevelopedToken();
                                break;
    
                        }

                        boardCell.token.cell = ficha;
                    }
                }
            } else
                boardCell.token = new UndevelopedToken();
            
            //define motion animation
            var apX:AnimateProperty = new AnimateProperty(boardCell.token);
            apX.fromValue = startPoint.x;
            apX.toValue = endPoint.x;
            apX.duration = 1000;
            apX.property = "x";
            var apY:AnimateProperty = new AnimateProperty(boardCell.token);
            apY.fromValue = startPoint.y;
            apY.toValue = endPoint.y;
            apY.duration = 1000;
            apY.property = "y";
            apY.addEventListener(EffectEvent.EFFECT_END, endUndoMove);

            //define size animation
            var apXScale:AnimateProperty = new AnimateProperty(boardCell.token);
            apXScale.property = "scaleX";
            apXScale.fromValue = startSize / endSize;
            apXScale.toValue = 1;
            apXScale.duration = 1000;
            var apYScale:AnimateProperty = new AnimateProperty(boardCell.token);
            apYScale.property = "scaleY";
            apYScale.fromValue = startSize / endSize;
            apYScale.toValue = 1;
            apYScale.duration = 1000;

            //start effect
            apX.play();
            apY.play();
            apXScale.play();
            apYScale.play();

            boardCell.reset();            
        }

        private function endUndoMove(event:EffectEvent):void {
            var token:ManantialesToken = ManantialesToken(AnimateProperty(event.currentTarget).target);
            if (_gameWindow.animateLayer.contains(token))
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
            if (!puzzleMode) {
                var checkConAlert:CheckConstraintAlert = new CheckConstraintAlert();
                checkConAlert.constraint = checkCondition;
                checkConAlert.raised = true;
                checkConAlert.addEventListener("result", handleCheckResult);
                _alerts.addItem(checkConAlert);
                PopUpManager.addPopUp(checkConAlert, _gameWindow, true);
                PopUpManager.centerPopUp(checkConAlert);
            }
        }

        private function handleCheckConstraintResolved (checkCondition:CheckCondition):void {
            if (!puzzleMode) {
                var checkConAlert:CheckConstraintAlert = new CheckConstraintAlert();
                checkConAlert.constraint = checkCondition;
                checkConAlert.raised = false;
                checkConAlert.addEventListener("result", handleCheckResult);
                _alerts.addItem(checkConAlert);
                PopUpManager.addPopUp(checkConAlert, _gameWindow, true);
                PopUpManager.centerPopUp(checkConAlert);
            }
        }

        private function handleCheckConstraintTriggered (checkCondition:CheckCondition):void {
            if (!puzzleMode) {
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

                /* Process the constraint in the token handler */
                for (var i:int = 0; i < checkCondition.violators.length; i++) {
                    var violator:Ficha = Ficha (checkCondition.violators.getItemAt(i));
                    _tokenHandler.processViolator(violator);
                }
            }
        }

        private function handleStateChange (game:ManantialesGame):void {
            if (_stageChangeAlert == null) {
                _stageChangeAlert = new GraphicAlert();
                _stageChangeAlert.text = "Stage complete. Progressing to next stage, '" +
                      game.mode + "'";
                _stageChangeAlert.positive = true;
                _stageChangeAlert.addEventListener ("result", handleStateChangeResult);
                _game = game;
                _moves = new ArrayCollection();
                _gameWindow.currentState =  _game.mode;
                _tokenHandler.resetTokenStores();

                /* Announce change */
                PopUpManager.addPopUp(_stageChangeAlert, _gameWindow, true);
                PopUpManager.centerPopUp(_stageChangeAlert);
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