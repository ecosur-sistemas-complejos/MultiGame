//copyright

package mx.ecosur.multigame.manantiales
{
    import flash.media.Sound;
    import flash.media.SoundChannel;
    import mx.collections.ArrayCollection;
    import mx.controls.Alert;
    import mx.core.IFlexDisplayObject;
    import mx.ecosur.multigame.component.SoundAssets;
    import mx.ecosur.multigame.component.Token;
    import mx.ecosur.multigame.entity.ChatMessage;
    import mx.ecosur.multigame.entity.GameGrid;
    import mx.ecosur.multigame.entity.GamePlayer;
    import mx.ecosur.multigame.enum.Color;
    import mx.ecosur.multigame.enum.ExceptionType;
    import mx.ecosur.multigame.event.DragMoveEvent;
    import mx.ecosur.multigame.manantiales.entity.CheckCondition;
    import mx.ecosur.multigame.manantiales.entity.Ficha;
    import mx.ecosur.multigame.manantiales.entity.ManantialesGame;
    import mx.ecosur.multigame.manantiales.entity.ManantialesMove;
    import mx.ecosur.multigame.manantiales.entity.ManantialesPlayer;
    import mx.ecosur.multigame.manantiales.entity.SimpleAgent;
    import mx.ecosur.multigame.manantiales.entity.Suggestion;
    import mx.ecosur.multigame.enum.GameEvent;
    import mx.ecosur.multigame.enum.GameState;
    import mx.ecosur.multigame.manantiales.enum.ConditionType;
    import mx.ecosur.multigame.manantiales.enum.ManantialesEvent;
    import mx.ecosur.multigame.manantiales.enum.Mode;
    import mx.ecosur.multigame.manantiales.enum.TokenType;
    import mx.ecosur.multigame.manantiales.token.*;
import mx.ecosur.multigame.manantiales.token.ManantialesToken;
import mx.ecosur.multigame.manantiales.token.UndevelopedToken;
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
        private var _expiredAlert:TimeExpiredAlert;
        private var _endAlert:GraphicAlert;

        private var _msgReceiver:MessageReceiver;
        private var _isEnded:Boolean;
        private var _messages:ArrayCollection;
        private var _sndChannel:SoundChannel;

        // constants
        protected static const MESSAGING_DESTINATION_NAME:String = "multigame-destination";
        protected static const GAME_SERVICE_DESTINATION_NAME:String = "gameService";
        protected static const GAME_SERVICE_GET_GRID_OP:String = "getGameGrid";
        protected static const GAME_SERVICE_GET_PLAYERS_OP:String = "getPlayers";
        protected static const GAME_SERVICE_GET_MOVES_OP:String = "getMoves";
        protected static const GAME_SERVICE_DO_MOVE_OP:String = "doMove";

        protected static const CLASSIC_WINNING_SCORE:int = 24;
        protected static const SILVO_WINNING_SCORE:int = 32;

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
            /* ensure the mode set to current state */
            move.mode = _game.mode;
            _gameWindow.gameStatus.showMessage(resourceManager.getString(
                    "Manantiales", "manantiales.move.processing"), Color.getColorCode(_currentPlayer.color));
            var call:Object = _gameService.doMove(_game, move);
            call.operation = "doMove";
            _executingMove = move;
            _gameWindow.currentState = "";
        }
        

        /*
         * Process the different types of messages received by the MessageReciever,
         * reordered and dispatched. All messages contain a game event
         * header, based on this different actions are taken.
         */
        private function processMessage(evt:DynamicEvent):void {
            _messages.addItem(evt);
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
                        updatePlayers(game);
                        handleGameEnd (game);
                        break;
                    case ManantialesEvent.MOVE_COMPLETE:
                        move= ManantialesMove(message.body);
                        _gameWindow.playersViewer.updatePlayers();
                        _moveHandler.addMove(move);
                        updatePlayersFromMove(move);
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
                    case ManantialesEvent.SUGGESTION_EVALUATED:
                        suggestion = Suggestion(message.body);
                        _suggestionHandler.addSuggestion (suggestion);
                        break;
                    case ManantialesEvent.SUGGESTION_APPLIED:
                        suggestion = Suggestion(message.body);
                        _suggestionHandler.removeSuggestion (suggestion);
                        break;
                    case GameEvent.EXPIRED:
                        move = ManantialesMove(message.body);
                        handleExpiredMove(move);
                        break;
                    default:
                        break;
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
            if (_game.state == GameState.PLAY) {
                _gameWindow.currentState= _game.mode;
                _tokenHandler.initializeTokenStores();
                _gameWindow.begin();
                if ( _game.mode == Mode.COMPETITIVE || _game.mode == Mode.SILVOPASTORAL || _game.turns == 0)
                    _currentPlayer.play();
            }
        }

        private function handleAnnCondResult(event:DynamicEvent):void{
            PopUpManager.removePopUp(_annCondGen);
            _annCondGen = null;
            if(!event.isGoodYear){
                /* retract tokens */
                _gameWindow.currentState = "";
                var move:ManantialesMove = new ManantialesMove ();
                move.badYear = true;
                move.player = _currentPlayer;
                move.mode = _game.mode;
                sendMove(move);
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
        }

        public function handleDragMoveEvent (event:DragMoveEvent):void {
            var m:ManantialesMove = ManantialesMove(event.move);
            m.player = _currentPlayer;
            m.mode = _game.mode;
            sendMove(m);
        }

        /**
         * Updates the list of players. Also updates the chat panel and
         * players viewer components.
         *
         * @param players the new list of players
         */
        public function updatePlayers(game:ManantialesGame):void {
            _game = game;
            _gameWindow.playersViewer.game = _game;
            var playerWithTurn:ManantialesPlayer;

            if (game.state != GameState.WAITING) {
                _gameWindow.timer.current = game.elapsedTime;
                _gameWindow.timer.displayTime(Color.getColorCode(_gameWindow.currentPlayer.color));
                _gameWindow.timer.flashMessage();

                if (game.mode == Mode.COMPETITIVE ||
                    game.mode == Mode.SILVOPASTORAL ||
                    game.mode == Mode.RELOADED)
                {
                    for (var i:int = 0; i < game.players.length; i++) {
                        var gamePlayer:ManantialesPlayer = ManantialesPlayer(game.players.getItemAt(i));
                        if (gamePlayer.id == _currentPlayer.id) {
                            _currentPlayer = gamePlayer;
                        }

                        if (gamePlayer.turn)
                            playerWithTurn = gamePlayer;
                    }

                    if (game.state == GameState.PLAY) {
                        if (_currentPlayer.turn) {
                            _gameWindow.gameStatus.showMessage(resourceManager.getString("Manantiales",
                                    "manantiales.currentplayer.turn"), Color.getColorCode(_currentPlayer.color));
                            initTurn();
                        } else {
                            _gameWindow.gameStatus.showMessage(
                                    playerWithTurn.name + " " + resourceManager.getString("Manantiales",
                                    "manantiales.tomove"), Color.getColorCode(gamePlayer.color));
                        }
                    }
                } else {
                    _gameWindow.gameStatus.showMessage(resourceManager.getString("Manantiales",
                        "manantiales.freeplay"), Color.getColorCode(_currentPlayer.color));
                    initTurn()
                }
            } else if (game.state == GameState.WAITING) {
                _gameWindow.gameStatus.showMessage(resourceManager.getString("Manantiales",
                "manantiales.wait.message"), Color.getColorCode(_currentPlayer.color));
            }

            _gameWindow.currentGame = game;
            _players = _game.players;
            _tokenHandler.update(_currentPlayer);
        }

        public function updatePlayersFromMove(move:ManantialesMove):void {
            if (move.mode == Mode.BASIC_PUZZLE || move.mode == Mode.SILVO_PUZZLE) {
                if (_gameWindow.currentState != move.mode)
                    _gameWindow.currentState = move.mode;
                _gameWindow.gameStatus.showMessage(resourceManager.getString("Manantiales",
                    "manantiales.freeplay"), Color.getColorCode(_currentPlayer.color));
                _gameWindow.playersViewer.setTurn(ManantialesPlayer(move.playerModel));
            }
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
                        _gameWindow.currentState = _game.mode;
                        _tokenHandler.initializeTokenStores();
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

        private function handleExpiredMove(move:ManantialesMove):void {
            if (_expiredAlert != null)
                PopUpManager.removePopUp(_expiredAlert);
            _expiredAlert = new TimeExpiredAlert();
            _expiredAlert.triggerMove = move;
            _expiredAlert.addEventListener("result", handleExpiredResult);
            PopUpManager.addPopUp(_expiredAlert,  _gameWindow,  true);
            PopUpManager.centerPopUp(_expiredAlert);
        }

        public function handleExpiredResult(event:DynamicEvent):void {
            PopUpManager.removePopUp(_expiredAlert);
            _expiredAlert = null;
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

        private function handleGameEnd(game:ManantialesGame):void {
            _game = game;

            /* Message the Window to END */
            _gameWindow.end();

            /* Remove all alert conditions from the PopUpManager */
            for (var i:int = 0; i < _alerts.length; i++)
                PopUpManager.removePopUp(IFlexDisplayObject(_alerts.getItemAt(i)));

            if (_endAlert == null) {
                _endAlert = new GraphicAlert();
                var expiredCondition:CheckCondition = null;
                var sound:Sound = null;

                /* See if any checkConstraints are expired and if so, Post a
                 different end condition alert */
                for (var i:int = 0; i < game.checkConditions.length; i++) {
                    var checkCondition:CheckCondition = CheckCondition(
                            game.checkConditions.getItemAt(i));
                    if (checkCondition.expired && (checkCondition.reason == ConditionType.MANANTIALES_DRY ||
                            checkCondition.reason == ConditionType.TERRITORY_DEFORESTED)) {
                        expiredCondition = checkCondition;
                    }
                }

                if (expiredCondition != null) {
                    _endAlert.text = resourceManager.getString("Manantiales", "manantiales.severe.expiration") +
                            " ('" + resourceManager.getString("Manantiales", expiredCondition.reason) + "')";
                    _endAlert.positive = false;
                } else {
                    if (puzzleMode) {
                        _endAlert.text = resourceManager.getString("Manantiales", "manantiales.all.winners");
                        _endAlert.positive = true;
                        _gameWindow.gameStatus.showMessage(resourceManager.getString(
                                "Manantiales", "manantiales.have.won"), 0x000000);
                    } else if (isWinner()) {
                        _endAlert.text = resourceManager.getString("Manantiales", "manantiales.competitive.win");
                        _endAlert.positive = true;
                        _gameWindow.gameStatus.showMessage(resourceManager.getString(
                                "Manantiales", "manantiales.have.won"), Color.getColorCode(_currentPlayer.color));
                        for (var j:int = 0; j < _gameWindow.board.boardCells.length; j++) {
                            var f:ManantialesToken = ManantialesToken(_gameWindow.board.boardCells[j]);
                        }
                    } else {
                        _endAlert.text = resourceManager.getString("Manantiales", "manantiales.competitive.lose");
                        _endAlert.positive = false;
                        _gameWindow.gameStatus.showMessage(resourceManager.getString(
                                "Manantiales", "manantiales.competitive.lose"), Color.getColorCode(_currentPlayer.color));
                    }
                }

                if (_endAlert.positive)
                    sound = SoundAssets.approval;
                else
                    sound = SoundAssets.failure;

                _endAlert.addEventListener("result", handleEndResult);
                PopUpManager.addPopUp(_endAlert, _gameWindow, true);
                PopUpManager.centerPopUp(_endAlert);
                _sndChannel = sound.play();
            }

            /* Remove message listener */
            destroy();
        }

        private function isWinner():Boolean {
            var ret:Boolean = false;
            switch (_game.mode) {
                case Mode.COMPETITIVE:
                    ret = _currentPlayer.score >= CLASSIC_WINNING_SCORE;
                    break;
                case Mode.SILVOPASTORAL:
                    ret = _currentPlayer.score >= SILVO_WINNING_SCORE;
                    break;
                case Mode.RELOADED:
                    // left for later implementation
                    break;
            }
            return ret;
        }

        public function destroy():void{
            _msgReceiver.destroy();
        }
    }
}
