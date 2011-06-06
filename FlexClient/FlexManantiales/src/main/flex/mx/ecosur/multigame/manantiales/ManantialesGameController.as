//copyright

package mx.ecosur.multigame.manantiales
{

    import mx.collections.ArrayCollection;
    import mx.controls.Alert;
    import mx.core.IFlexDisplayObject;
    import mx.ecosur.multigame.component.Token;
    import mx.ecosur.multigame.entity.ChatMessage;
    import mx.ecosur.multigame.entity.GameGrid;
    import mx.ecosur.multigame.entity.GamePlayer;
    import mx.ecosur.multigame.enum.Color;
    import mx.ecosur.multigame.enum.ExceptionType;
    import mx.ecosur.multigame.entity.manantiales.CheckCondition;
    import mx.ecosur.multigame.entity.manantiales.Ficha;
    import mx.ecosur.multigame.entity.manantiales.ManantialesGame;
    import mx.ecosur.multigame.entity.manantiales.ManantialesMove;
    import mx.ecosur.multigame.entity.manantiales.ManantialesPlayer;
    import mx.ecosur.multigame.entity.manantiales.SimpleAgent;
    import mx.ecosur.multigame.entity.manantiales.Suggestion;
    import mx.ecosur.multigame.manantiales.enum.ConditionType;
    import mx.ecosur.multigame.manantiales.enum.ManantialesEvent;
    import mx.ecosur.multigame.enum.manantiales.Mode;
    import mx.ecosur.multigame.enum.manantiales.TokenType;
    import mx.ecosur.multigame.manantiales.token.*;
    import mx.ecosur.multigame.util.MessageReceiver;
    import mx.events.CloseEvent;
    import mx.events.DynamicEvent;
    import mx.managers.PopUpManager;
    import mx.messaging.messages.ErrorMessage;
    import mx.messaging.messages.IMessage;
    import mx.resources.IResourceManager;
    import mx.resources.ResourceManager;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;
    import mx.rpc.remoting.RemoteObject;

    [ResourceBundle("Manantiales")]
    public class ManantialesGameController
    {
        public var _gameWindow:ManantialesWindow;
        public var _suggestionHandler:SuggestionHandler;
        public var _tokenHandler:TokenHandler;
        public var _moveHandler:MoveHandler;
        public var _gameService:RemoteObject;

        public var _currentPlayer:ManantialesPlayer;
        public var _game:ManantialesGame;
        public var _moves:ArrayCollection;
        public var _selectedMoveInd:Number;

        public var _executingMove:ManantialesMove;
        public var _isTurn:Boolean;
        public var _previousToken:Token;        

        private var _players:ArrayCollection;
        private var _gameId:int;
        private var _annCondGen:AnnualConditionsGenerator;
        private var _alerts:ArrayCollection;
        private var _endAlert:GraphicAlert;
        private var _stageChangeAlert:GraphicAlert;

        private var _msgReceiver:MessageReceiver;
        private var _isEnded:Boolean;
        private var _stateChange:Boolean;
        private var _messages:ArrayCollection;

        // constants
        private static const MESSAGING_DESTINATION_NAME:String = "multigame-destination";
        private static const GAME_SERVICE_DESTINATION_NAME:String = "gameService";
        private static const GAME_SERVICE_GET_GRID_OP:String = "getGameGrid";
        private static const GAME_SERVICE_GET_PLAYERS_OP:String = "getPlayers";
        private static const GAME_SERVICE_GET_MOVES_OP:String = "getMoves";
        private static const GAME_SERVICE_DO_MOVE_OP:String = "doMove";

        /* Needed to force compilation of SimplePlayer in .swf file */
        private var _unUsed:SimpleAgent;

        /* Internationalization */
        private var resourceManager:IResourceManager = ResourceManager.getInstance();
        

        public function ManantialesGameController (gameWindow:ManantialesWindow)
        {
            _messages = new ArrayCollection();

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
            _moveHandler = new MoveHandler (this);               

            // initialize game status
            _gameWindow.gameStatus.showMessage(resourceManager.getString("Manantiales", "manantiales.welcome") + " " +
                _currentPlayer.name + ".\n\n" + resourceManager.getString("Manantiales",
                    "manantiales.identify") + " " + 
                Color.getColorDescription(_currentPlayer.color),
                Color.getColorCode(_currentPlayer.color));

                        // initialize the board
            _gameWindow.board.dragEnterHandler = _moveHandler.dragEnterBoardCell;
            _gameWindow.board.dragDropHandler = _tokenHandler.dragDropCell;
            _gameWindow.board.dragExitHandler = _tokenHandler.dragExitCell;

                        // initialize the move viewer
            _gameWindow.moveViewer.addEventListener(MoveViewer.MOVE_EVENT_GOTO_MOVE, _moveHandler.gotoMove);
            _gameWindow.moveViewer.board = _gameWindow.board;

            // get the game grid, players and moves
            var callGrid:Object = _gameService.getGameGrid(_gameId);
            callGrid.operation = GAME_SERVICE_GET_GRID_OP;
            var callPlayers:Object = _gameService.getPlayers(_gameId);
            callPlayers.operation = GAME_SERVICE_GET_PLAYERS_OP;
            var callMoves:Object = _gameService.getMoves(_gameId, _game.mode);
            callMoves.operation = GAME_SERVICE_GET_MOVES_OP;
        }

        
        public function get puzzleMode():Boolean {
            return (_game.mode == "BASIC_PUZZLE" || _game.mode == "SILVO_PUZZLE");
        }

        public function sendMove (move:ManantialesMove):void {
            /* forcibly set the mode to that current state */
            move.mode = _game.mode;
            var call:Object = _gameService.doMove(_game, move);
            call.operation = "doMove";
            _executingMove = move;
        }
        

        /*
         * Process the different types of messages received by the MessageReciever,
         * reordered and dispatched. All messages contain a game event
         * header, based on this different actions are taken.
         */
        private function processMessage(evt:DynamicEvent):void {
            _messages.addItem(evt);

            if (!_stateChange) {
                for (var i:int = 0; i < _messages.length; i++) {
                    var event:DynamicEvent = DynamicEvent(_messages.getItemAt(i));
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
                            if(chatMessage.sender.id != _currentPlayer.id){
                                _gameWindow.gameStatus.showMessage(
                                    chatMessage.sender.name + " " +
                                    resourceManager.getString("Manantiales",
                                            "manantiales.panel.chat.announcement"), 0x000000);
                            }
                            break;
                        case ManantialesEvent.END:
                            game = ManantialesGame(message.body);
                            handleGameEnd (game);
                            break;
                        case ManantialesEvent.MOVE_COMPLETE:
                            move= ManantialesMove(message.body);
                            _gameWindow.playersViewer.updatePlayers();
                            _moveHandler.addMove(move);
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

                _messages = new ArrayCollection();
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
                        if (!_moves)
                            _moves = new ArrayCollection();
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
            _gameWindow.gameStatus.showMessage(resourceManager.getString("Manantiales",
                "manantiales.start.message"), 0x000000);
        }

        /*
         * Called when game ends
         */
        private function end():void{

            _gameWindow.gameStatus.showMessage(resourceManager.getString("Manantiales","manantiales.have.won"),
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
            if (_game.grid && _game.grid.cells && _game.grid.cells.length > 0) {
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

            _currentPlayer.play();
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

        private function handleEndResult (event:DynamicEvent):void {
            PopUpManager.removePopUp(this._endAlert);
            _endAlert = null;
            this._gameWindow.dispatchCompletion();
        }

        private function endTurn():void {                                    
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
        public function updatePlayers(game:ManantialesGame):void {
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
                        _gameWindow.gameStatus.showMessage(resourceManager.getString("Manantiales",
                                "manantiales.currentplayer.turn"), Color.getColorCode(_currentPlayer.color));
                    }else{
                        _gameWindow.gameStatus.showMessage(
                            gamePlayer.name + " " + resourceManager.getString("Manantiales","manantiales.tomove"),
                                    Color.getColorCode(gamePlayer.color));
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

        /*
         * Handles faults from game service calls
         */
        private function gameServiceFaultHandler(event:FaultEvent):void{
            var errorMessage:ErrorMessage = ErrorMessage(event.message);
            if (errorMessage.extendedData != null){
                if(errorMessage.extendedData[ExceptionType.EXCEPTION_TYPE_KEY] == ExceptionType.INVALID_MOVE){
                    var fnc:Function = function (event:CloseEvent):void{
                        _moveHandler.invalidMove(_executingMove);
                    }
                    Alert.show(resourceManager.getString("Manantiales","manantiales.move.invalid"),
                            resourceManager.getString("Manantiales","manantiales.move.error.title"), Alert.OK, null,
                            fnc);
                }
            }else{
                Alert.show(event.fault.faultString, resourceManager.getString("Manantiales",
                        "manantiales.controller.server.error"));
            }
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
            _stateChange = true;
            _gameWindow.currentState = "";
            _game = game;
            if (_stageChangeAlert == null) {
                /* Set the mode */
                _gameWindow.currentState = "";
                var winner:Boolean = _isTurn;

                /* update state */
                updatePlayers (_game);
                
                _stageChangeAlert = new GraphicAlert();
                if (_game.mode == Mode.BASIC_PUZZLE || _game.mode == Mode.SILVO_PUZZLE) {
                    if (winner) {
                        _stageChangeAlert.text = " '" +
                            game.mode + "'";
                        _stageChangeAlert.positive = true;

                    } else {
                        _stageChangeAlert.text = resourceManager.getString("Manantiales",
                                "manantiales.competitive.lose") + " '" +
                            game.mode + "'";
                        _stageChangeAlert.positive = false;
                    }
                } else {
                    _stageChangeAlert.text = resourceManager.getString("Manantiales","manantiales.puzzle.solved") +
                            " '" + game.mode + "'";
                    _stageChangeAlert.positive = true;
                }

                _stageChangeAlert.addEventListener ("result", handleStateChangeResult);

                /* Announce change */
                PopUpManager.addPopUp(_stageChangeAlert, _gameWindow, true);
                PopUpManager.centerPopUp(_stageChangeAlert);

                _stageChangeAlert.play();
            }
        }

        private function handleStateChangeResult (event:DynamicEvent):void {
            PopUpManager.removePopUp(_stageChangeAlert);
            _stageChangeAlert = null;

            _gameWindow.currentState = _game.mode;

            /* Clear moves for next stage  */
            _moves = new ArrayCollection();
                        
            _gameWindow.moveViewer.board = _gameWindow.board;
            _gameWindow.moveViewer.initFromMoves(_moves);
            _selectedMoveInd = _moves.length - 1;

            _gameWindow.invalidateDisplayList();
            _gameWindow.leftBox.invalidateDisplayList();
            _gameWindow.leftBox.invalidateProperties();
            _gameWindow.invalidateSize();
            _tokenHandler.resetTokenStores();

            /* Init Game Grid with clear grid */
            _stateChange = false;
            _msgReceiver.destroy();

            /* Reinitialize the message reciever */
            _msgReceiver = new MessageReceiver(MESSAGING_DESTINATION_NAME, _game.id);
            _msgReceiver.addEventListener(MessageReceiver.PROCESS_MESSAGE, processMessage);

            /* Reset game state */
            _gameWindow.currentState = _game.mode;

            // get the game grid, players and moves
            var callGrid:Object = _gameService.getGameGrid(_gameId);
            callGrid.operation = GAME_SERVICE_GET_GRID_OP;
            var callPlayers:Object = _gameService.getPlayers(_gameId);
            callPlayers.operation = GAME_SERVICE_GET_PLAYERS_OP;
            var callMoves:Object = _gameService.getMoves(_gameId, _game.mode);
            callMoves.operation = GAME_SERVICE_GET_MOVES_OP;
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
                          _endAlert.text = resourceManager.getString("Manantiales","manantiales.severe.expiration") +
                                  " ('" + expiredCondition.reason + "')";
                          _endAlert.positive = false;
                        } else {
                            _endAlert.text = resourceManager.getString("Manantiales","manantiales.all.winners");
                            _endAlert.positive = true;
                            end();
                        }

                    _endAlert.addEventListener("result",handleEndResult);
                }

                    /* Remove all alert conditions from the PopUpManager */
                for (i = 0; i < _alerts.length; i++)
                        PopUpManager.removePopUp(IFlexDisplayObject(_alerts.getItemAt(i)));

            PopUpManager.addPopUp(_endAlert, _gameWindow, true);
            PopUpManager.centerPopUp(_endAlert);
        }

        public function destroy():void{
            _msgReceiver.destroy();
        }
    }
}
