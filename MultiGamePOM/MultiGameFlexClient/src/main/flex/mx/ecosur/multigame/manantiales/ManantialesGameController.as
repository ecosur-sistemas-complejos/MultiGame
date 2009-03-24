package mx.ecosur.multigame.manantiales
{	
    import flash.events.MouseEvent;
    import flash.geom.Point;
    
    import mx.collections.ArrayCollection;
    import mx.controls.Alert;
    import mx.core.DragSource;
    import mx.core.IFlexDisplayObject;
    import mx.core.UIComponent;
    import mx.ecosur.multigame.component.BoardCell;
    import mx.ecosur.multigame.component.ChatPanel;
    import mx.ecosur.multigame.component.GameStatus;
    import mx.ecosur.multigame.component.MoveViewer;
    import mx.ecosur.multigame.component.PlayersViewer;
    import mx.ecosur.multigame.component.Token;
    import mx.ecosur.multigame.entity.Cell;
    import mx.ecosur.multigame.entity.ChatMessage;
    import mx.ecosur.multigame.entity.Game;
    import mx.ecosur.multigame.entity.GameGrid;
    import mx.ecosur.multigame.entity.GamePlayer;
    import mx.ecosur.multigame.entity.Move;
    import mx.ecosur.multigame.enum.Color;
    import mx.ecosur.multigame.enum.ExceptionType;
    import mx.ecosur.multigame.enum.GameEvent;
    import mx.ecosur.multigame.util.MessageReceiver;
    import mx.effects.AnimateProperty;
    import mx.events.CloseEvent;
    import mx.events.DragEvent;
    import mx.events.DynamicEvent;
    import mx.events.EffectEvent;
    import mx.managers.DragManager;
    import mx.messaging.messages.ErrorMessage;
    import mx.messaging.messages.IMessage;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;
    import mx.rpc.remoting.RemoteObject;
	
	public class ManantialesGameController
	{	
		
		        // visual components
        private var _board:ManantialesBoard;
        private var _chatPanel:ChatPanel;
        private var _gameStatus:GameStatus;
        private var _animateLayer:UIComponent;
        private var _forestStore:ManantialesTokenStore;
        private var _moderateStore:ManantialesTokenStore;
        private var _intensiveStore:ManantialesTokenStore;
        private var _viveroStore:ManantialesTokenStore;
        private var _silvoStore:ManantialesTokenStore;
                
        // data objects
        private var _gameId:int;
        private var _currentPlayer:GamePlayer;
        private var _players:ArrayCollection;
        private var _playersViewer:PlayersViewer;
        private var _game:Game;
        private var _tokens:ArrayCollection;
        private var _moves:ArrayCollection;
        private var _moveViewer:MoveViewer;        
        private var _selectedMoveInd:Number; 
        private var _gameGrid:GameGrid;
        
        // server objects
        private var _gameService:RemoteObject;
        private var _msgReceiver:MessageReceiver;
        
        // flags
        private var _isMoving:Boolean;
        private var _executingMove:Move;
        private var _isTurn:Boolean;
        private var _isEnded:Boolean;
        
        // constants
        private static const TOKEN_STORE_MIN_WIDTH:int = 150;
        private static const TOKEN_STORE_MAX_WIDTH:int = 300;
        private static const N_TOKENS_IN_STORE:int = 50;
        private static const MESSAGING_DESTINATION_NAME:String = "multigame-destination";
        private static const GAME_SERVICE_DESTINATION_NAME:String = "gameService";
        private static const GAME_SERVICE_GET_GRID_OP:String = "getGameGrid";
        private static const GAME_SERVICE_GET_PLAYERS_OP:String = "getPlayers";
        private static const GAME_SERVICE_GET_MOVES_OP:String = "getMoves";
        private static const GAME_SERVICE_UPDATE_MOVE_OP:String = "updateMove";
        private static const GAME_SERVICE_DO_MOVE_OP:String = "doMove";
		
		
        public function ManantialesGameController(currentPlayer:GamePlayer, 
            board:ManantialesBoard, chatPanel:ChatPanel, 
            playersViewer:PlayersViewer, 
            forestStore:ManantialesTokenStore, intensiveStore:ManantialesTokenStore, 
            moderateStore:ManantialesTokenStore, viveroStore:ManantialesTokenStore, 
            silvoStore:ManantialesTokenStore, gameStatus:GameStatus, 
            moveViewer:MoveViewer, 
            animateLayer:UIComponent)
        {
            super();
            
            // set private references
            _gameId = currentPlayer.game.id;
            _currentPlayer = currentPlayer;
            _board = board;
            _chatPanel = chatPanel;
            _forestStore = forestStore;
            _intensiveStore = intensiveStore;
            _moderateStore = moderateStore;
            _viveroStore = viveroStore;
            _silvoStore = silvoStore;
            _gameStatus = gameStatus;
            _animateLayer = animateLayer;
            _moves = new ArrayCollection();
            _playersViewer = playersViewer;
            _moveViewer = moveViewer;
            
            // initialize game service remote object
            _gameService = new RemoteObject();
            _gameService.destination = GAME_SERVICE_DESTINATION_NAME;
            _gameService.addEventListener(ResultEvent.RESULT, gameServiceResultHandler);
            _gameService.addEventListener(FaultEvent.FAULT, gameServiceFaultHandler);
            
            // initialize message receiver
            _msgReceiver = new MessageReceiver(MESSAGING_DESTINATION_NAME, _currentPlayer.game.id);
            _msgReceiver.addEventListener(MessageReceiver.PROCESS_MESSAGE, processMessage);
            
            // initialize token stores
            _forestStore.startMoveHandler = startMove;
            _forestStore.endMoveHandler = endMove;
            _forestStore.active = false;
            
            _intensiveStore.startMoveHandler = startMove;
            _intensiveStore.endMoveHandler = endMove;
            _intensiveStore.active = false;
            
            _moderateStore.startMoveHandler = startMove;
            _moderateStore.endMoveHandler = endMove;
            _moderateStore.active = false;
                                   
            _viveroStore.startMoveHandler = startMove;
            _viveroStore.endMoveHandler = endMove;
            _viveroStore.active = false;
                                    
            _silvoStore.startMoveHandler = startMove;
            _silvoStore.endMoveHandler = endMove;
            _silvoStore.active = false;
            
            // initialize game status
            _gameStatus.showMessage("Welcome to the game " + 
                currentPlayer.player.name + "!\n\n You are " + 
                Color.getColorDescription(currentPlayer.color), 
                Color.getColorCode(currentPlayer.color));
            
            // initialize the move viewer
            _moveViewer.addEventListener(MoveViewer.MOVE_EVENT_GOTO_MOVE, gotoMove);
            _moveViewer.board = _board;
            
            // initialize the board
            _board.dragEnterHandler = dragEnterBoardCell;
            _board.dragDropHandler = dragDropCell;
            _board.dragExitHandler = dragExitCell;                          
            
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
                
                var token:Token = Token(evt.dragSource.dataForFormat("token"));
                var boardCell:RoundCell = RoundCell(evt.currentTarget);
                boardCell.addEventListener(DragEvent.DRAG_EXIT, dragExitCell);
                
                // calculate if move is valid
                if (validateMove(boardCell, token)){
                    boardCell.select(token.cell.colorCode);
                    DragManager.acceptDragDrop(boardCell);
                }
            }
        }
        
        private function validateMove(boardCell:RoundCell, token:Token):Boolean
        {
        	var ret:Boolean = false;
            
            if (boardCell.token == null && 
                (token.cell.color == boardCell.color || boardCell.color == Color.UNKNOWN))
            {
                ret = true;
            }
            
            return ret;   
        }        
        
        private function startMove(evt:MouseEvent):void{
            
            if (!_isMoving && _isTurn){
            
                // initialize drag source
                var token:Token = Token(evt.currentTarget);
                var ds:DragSource = new DragSource();
                ds.addData(token, "token");
                                
                // create proxy image and start drag
                var dragImage:IFlexDisplayObject = token.createDragImage();
                DragManager.doDrag(token, ds, evt, dragImage);
                _isMoving = true;
                
            }
        } 
        
        private function endMove(evt:DragEvent):void{
            
            if (evt.dragSource.hasFormat("token")){
                _isMoving = false;
                var token:Token = Token(evt.currentTarget);
                token.selected = false;
            }
        }    
        
        
        /* Go directly to a given move in the move history of the game.
         * Animates tokens on or off the board to transform the current
         * board into a snapshot of the desired move.
         */
        private function gotoMove(event:DynamicEvent):void{

            var move:Move = Move(event.move);
            
            // if move is before the currently selected move then iterate
            // back over the moves transforming the board
            // else iterate forward
            if(move.id < Move(_moves[_selectedMoveInd]).id){
                do{
                    undoMove(Move(_moves[_selectedMoveInd]));
                    _selectedMoveInd --;                    
                }while(move.id < Move(_moves[_selectedMoveInd]).id 
                        && _selectedMoveInd > 0);
            }else if (move.id > Move(_moves[_selectedMoveInd]).id 
                        && _selectedMoveInd < _moves.length){
                do{
                    doMove(Move(_moves[_selectedMoveInd + 1]));
                    _selectedMoveInd ++;
                } while (move.id > Move(_moves[_selectedMoveInd]).id 
                    && _selectedMoveInd < _moves.length);
            }
        }            
        
        public function dragDropCell(evt:DragEvent):void{
            
            if (evt.dragSource.hasFormat("token")){
                
                // define cell and destination
                var token:Token = Token (evt.dragSource.dataForFormat("token"));
                var boardCell:BoardCell = BoardCell(evt.currentTarget);
                var destination:Cell = token.cell.clone();
                destination.row = boardCell.row;
                destination.column = boardCell.column;
                
                // do move in backend
                var move:Move = new Move();
                move.player = _currentPlayer;
                move.destination = destination;
                move.status = Move.UNVERIFIED;
                var call:Object = _gameService.doMove(move);
                call.operation = GAME_SERVICE_DO_MOVE_OP;
                _executingMove = move; 
                
                // do move in interface
                boardCell.reset();
                
                var newToken:Token
                if (token is ForestToken) {
                    _forestStore.removeToken();
                    newToken = new ForestToken();
                }
                else if (token is IntensiveToken) {
                    _intensiveStore.removeToken();
                    newToken = new IntensiveToken();
                }
                else if (token is ModerateToken) { 
                    _moderateStore.removeToken();
                    newToken = new ModerateToken();
                }
                else if (token is ViveroToken) {
                	_viveroStore.removeToken();
                	newToken = new ViveroToken();
                }
                else if (token is SilvopastoralToken) {
                	_silvoStore.removeToken();
                	newToken = new SilvopastoralToken();
                }

                newToken.cell = destination;
                _board.addToken(newToken);
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
            var gameId:Number = message.headers.GAME_ID;
            var gameEvent:String = message.headers.GAME_EVENT;
        
            switch (gameEvent){
                case GameEvent.BEGIN:
                    begin();
                    break;
                case GameEvent.CHAT:
                    var chatMessage:ChatMessage = ChatMessage(message.body); 
                    _chatPanel.addMessage(chatMessage);
                    if(chatMessage.sender.id != _currentPlayer.player.id){
                        _gameStatus.showMessage(chatMessage.sender.player.name + " has sent you a message", 0x000000);
                    }
                    break;
                case GameEvent.END:
                    if (_isTurn){
                        end();
                    }
                    break;
                case GameEvent.MOVE_COMPLETE:
                    var move:Move = Move(message.body);
                    addMove(move);
                    break;
                case GameEvent.PLAYER_CHANGE:
                    var players:ArrayCollection = ArrayCollection(message.body);
                    updatePlayers(players);
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
                    updatePlayers(ArrayCollection(event.result));
                    break;
                case GAME_SERVICE_GET_MOVES_OP:
                    _moves = ArrayCollection(event.result);
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
            _gameStatus.showMessage("All players have joined. The game will now begin.", 
                0x00000);  
        }  
        
        /*
         * Called when game ends 
         */
        private function end():void{
            
            if(_isEnded){
                return;
            }
            
            // remove the token store
            _forestStore.active = false;
            _moderateStore.active = false;
            _intensiveStore.active = false;
            _viveroStore.active = false;
            _silvoStore.active = false;            
            _isEnded = true;
            
        }
        
        private function getGrid():void{
            var call:Object = _gameService.getGameGrid(_gameId);
            call.operation = "getGameGrid";
        }
        
        private function initGrid(gameGrid:GameGrid):void{
            var cell:Cell;
            var token:Token;
            _gameGrid = gameGrid;
            _board.clearTokens();
            if (_gameGrid.cells && _gameGrid.cells.length > 0){
                for (var i:Number = 0; i < _gameGrid.cells.length; i++){
                    cell = Cell(_gameGrid.cells[i]);
                    token = new Token();
                    token.cell = cell;
                    _board.addToken(token);
                }
            }
        } 
        
        private function initTurn():void{
            _forestStore.active = true;
            _moderateStore.active = true;
            _intensiveStore.active = true;
            _viveroStore.active = true;
            _silvoStore.active = true;  
        }
        
        private function endTurn():void{
            _forestStore.active = false;
            _moderateStore.active = false;
            _intensiveStore.active = false;
            _viveroStore.active = false;
            _silvoStore.active = false;  
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
        public function updatePlayers(players:ArrayCollection):void{
            var gamePlayer:GamePlayer;
            for (var i:int = 0; i < players.length; i++){
                gamePlayer = GamePlayer(players[i]); 
                if (gamePlayer.id == _currentPlayer.id){
                    _currentPlayer = gamePlayer;
                    _chatPanel.currentPlayer = _currentPlayer;
                    this.isTurn = _currentPlayer.turn;
                }
                if (gamePlayer.turn){
                    if (gamePlayer.id == _currentPlayer.id){
                        _gameStatus.showMessage("Its your turn", 
                            Color.getColorCode(_currentPlayer.color));
                    }else{
                        _gameStatus.showMessage(
                            gamePlayer.player.name + " to move", Color.getColorCode(gamePlayer.color));
                    }
                }
            }
            
            _playersViewer.players = players;
            _players = players;
        }
        
        private function addMove(move:Move):void{
            
            //get last move in game
            var lastMove:Move = null;
            if (_moves.length > 0){
                lastMove = Move(_moves[length - 1]);
            }
            
            //if move is after the last move then add moves
            //else update the move since its info may have changed
            if (lastMove == null || move.id > lastMove.id){
                
                //add to moves
                _moves.source.push(move);
                
                //if current move is the last move then animate
                if (_selectedMoveInd == _moves.length - 2){
                    _selectedMoveInd ++;
                    doMove(move);
                }
            }else{
                
                // Search for move in reverse order because its most likely to be the last move
                var oldMove:Move;
                for (var i:Number = _moves.length - 1; i >= 0; i--){
                    oldMove = Move(_moves[i]);
                    if (oldMove.id == move.id){
                        _moves[i] = move;
                        break;
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
        private function doMove(move:Move):void{
            
            //check that destination is free
            var boardCell:BoardCell = _board.getBoardCell(move.destination.column, move.destination.row);
            
            
            
            
            //define origin
            var startPoint:Point;
            var startSize:Number;
                /* HACK:  use the forestStore as the origin for the move,
                this may need to change over time */
            if(move.player.id == _currentPlayer.id && _isTurn){
                startPoint = new Point(_forestStore.width, _forestStore.height);
                startPoint = _forestStore.localToGlobal(startPoint);
                startPoint = _animateLayer.globalToLocal(startPoint);
                startSize = _board.tokenSize;
            }
            
            //define destination
            var endPoint:Point = new Point(boardCell.width / 2, boardCell.height / 2);
            var endSize:Number = _board.tokenSize;
            endPoint = boardCell.localToGlobal(endPoint);
            endPoint = _animateLayer.globalToLocal(endPoint);
            
            //create new token
            var token:Token = new Token();
            token.cell = move.destination;
            token.width = endSize;
            token.height = endSize;
            _animateLayer.addChild(token);
                        
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
            
            var token:Token = Token(AnimateProperty(event.currentTarget).target);
            var boardCell:BoardCell = _board.getBoardCell(token.cell.column, token.cell.row);
            _animateLayer.removeChild(token);
            
            //remove from token store if necessary
            if(token.cell.color == _currentPlayer.color && _isTurn){
            	if (token is ForestToken) {
                    _forestStore.removeToken();
                }
                else if (token is IntensiveToken) {
                    _intensiveStore.removeToken();   
                }
                else if (token is ModerateToken) { 
                    _moderateStore.removeToken();
                }
                else if (token is ViveroToken) {
                    _viveroStore.removeToken();
                }
                else if (token is SilvopastoralToken) {
                    _silvoStore.removeToken();
                }
            }
            
            boardCell.token = token;
            boardCell.token.blink(1);
            
            // Update move viewer
            var move:Move = Move(_moves[_selectedMoveInd])            
        }
        
        private function undoMove(move:Move):void{
            
            //define origin
            var boardCell:BoardCell = _board.getBoardCell(move.destination.column, move.destination.row);
            var startPoint:Point = new Point(boardCell.width / 2, boardCell.height / 2);
            var startSize:Number = _board.tokenSize;
            startPoint = boardCell.localToGlobal(startPoint);
            startPoint = _animateLayer.globalToLocal(startPoint);
            
            //define destination
            var endPoint:Point;
            var endSize:Number;
                /* HACK:  forestStore is used as the reference. */  
            if(move.player.id == _currentPlayer.id && _isTurn){
                endPoint = new Point(_forestStore.width / 2, _forestStore.height / 2);
                endPoint = _forestStore.localToGlobal(endPoint);
                endPoint = _animateLayer.globalToLocal(endPoint);
                endSize = _board.tokenSize;
            }
            
            //create new token
            var token:Token = new Token();
            token.cell = move.destination;
            token.width = endSize;
            token.height = endSize;
            boardCell.token = null;
            boardCell.reset();
            _animateLayer.addChild(token);

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
        
        private function endUndoMove(event:EffectEvent):void{
            
            var token:Token = Token(AnimateProperty(event.currentTarget).target);
            _animateLayer.removeChild(token);
            
            //add to token store if necessary
            if(token.cell.color == _currentPlayer.color && _isTurn){
                if (token is ForestToken) {
                    _forestStore.addToken();
                }
                else if (token is IntensiveToken) {
                    _intensiveStore.addToken();
                }
                else if (token is ModerateToken) { 
                    _moderateStore.addToken();
                }
                else if (token is ViveroToken) {
                    _viveroStore.addToken();
                }
                else if (token is SilvopastoralToken) {
                    _silvoStore.addToken();
                }
            }
        }        
        
        public function destroy():void{
            _msgReceiver.destroy();
        }

	}
}