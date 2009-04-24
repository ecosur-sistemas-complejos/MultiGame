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
    import mx.core.UIComponent;
    import mx.ecosur.multigame.component.BoardCell;
    import mx.ecosur.multigame.component.ChatPanel;
    import mx.ecosur.multigame.component.GameStatus;
    import mx.ecosur.multigame.component.Token;
    import mx.ecosur.multigame.entity.ChatMessage;
    import mx.ecosur.multigame.entity.Game;
    import mx.ecosur.multigame.entity.GameGrid;
    import mx.ecosur.multigame.entity.GamePlayer;
    import mx.ecosur.multigame.entity.Move;
    import mx.ecosur.multigame.enum.Color;
    import mx.ecosur.multigame.enum.ExceptionType;
    import mx.ecosur.multigame.enum.GameEvent;
    import mx.ecosur.multigame.exception.CheckConstraint;
    import mx.ecosur.multigame.manantiales.entity.Ficha;
    import mx.ecosur.multigame.manantiales.entity.ManantialesMove;
    import mx.ecosur.multigame.manantiales.entity.ManantialesPlayer;
    import mx.ecosur.multigame.manantiales.enum.TokenType;
    import mx.ecosur.multigame.manantiales.token.*;
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
        private var _tokenStorePanels:ArrayCollection;
                
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
        private var _executingMove:ManantialesMove;
        private var _isTurn:Boolean;
        private var _isEnded:Boolean;
        private var _previousToken:Token;
        
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
        
        public function ManantialesGameController (gameWindow:ManantialesWindow) 
        {
        	
        	// set private references
        	_currentPlayer = gameWindow.currentPlayer;
            _gameId = _currentPlayer.game.id;
            _board = gameWindow.board;
            _chatPanel = gameWindow.chatPanel;
            _forestStore = gameWindow.forestStore;
            _intensiveStore = gameWindow.intensiveStore;
            _moderateStore = gameWindow.moderateStore;
            _viveroStore = gameWindow.viveroStore;
            _silvoStore = gameWindow.silvoStore;
            _gameStatus = gameWindow.gameStatus;
            _animateLayer = gameWindow.animateLayer;
            _playersViewer = gameWindow.playersViewer;
            _moveViewer = gameWindow.moveViewer;
            
                // instantiate collections 
            _moves = new ArrayCollection();
            _tokenStorePanels = new ArrayCollection();
            
               // setup token store panels for hiding 
            _tokenStorePanels.addItem(gameWindow.mfp);
            _tokenStorePanels.addItem(gameWindow.mgp);
            _tokenStorePanels.addItem(gameWindow.igp);
            _tokenStorePanels.addItem(gameWindow.sep);
            _tokenStorePanels.addItem(gameWindow.sap);
        	
             // initialize game service remote object
            _gameService = new RemoteObject();
            _gameService.destination = GAME_SERVICE_DESTINATION_NAME;
            _gameService.addEventListener(ResultEvent.RESULT, 
                gameServiceResultHandler);
            _gameService.addEventListener(FaultEvent.FAULT, 
                gameServiceFaultHandler);
            
            // initialize message receiver
            _msgReceiver = new MessageReceiver(MESSAGING_DESTINATION_NAME, 
                _currentPlayer.game.id);
            _msgReceiver.addEventListener(MessageReceiver.PROCESS_MESSAGE, 
                processMessage);
            
            // initialize token stores
            for (var i:int = 0; i < _tokenStorePanels.length; i++) {
            	initializeTokenStorePanel (Panel(_tokenStorePanels.getItemAt(i)));
            }
            
            // initialize game status
            _gameStatus.showMessage("Welcome to the game " + 
                _currentPlayer.player.name + "!\n\n You are " + 
                Color.getColorDescription(_currentPlayer.color), 
                Color.getColorCode(_currentPlayer.color));
            
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
            
            // setup display
            for (var j:int = 0; j < this._tokenStorePanels.length; j++) {
                var obj:Object = _tokenStorePanels.getItemAt(j);
                if (obj is Panel) {
                    var pnl:Panel = Panel(obj);             
                    if (pnl.visible)
                        pnl.visible = false;
                }
            }
            
            this._playersViewer.autoLayout = true;         	        	
        }
        
        public function initializeTokenStorePanel (tokenStorePanel:Panel):void {
        	var _store:ManantialesTokenStore = ManantialesTokenStore(
        	   tokenStorePanel.getChildAt(0));        	
        	_store.startMoveHandler = startMove;
            _store.endMoveHandler = endMove;
            _store.active = false;
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
		        } else if (boardCell.color == Color.UNKNOWN) {
		        	switch (token.cell.color) {
		        		case Color.BLUE:
		        		     if (boardCell.column < 5 && boardCell.row < 5)
		        		         ret = true;
		        		     break;
	                    case Color.GREEN:
	                         if (boardCell.column > 3 && boardCell.row < 5)
	                             ret = true;
	                         break;
	                    case Color.YELLOW:
	                         if (boardCell.column > 3 && boardCell.row > 3)
	                             ret = true;
	                         break;
	                    case Color.RED:
	                         if (boardCell.column < 5 && boardCell.row > 3)
	                             ret = true;
	                         break;                                                  
		        	}
		        }
            } else if (! (boardCell.token is IntensiveToken)) {
            	if (token.cell.color == boardCell.token.cell.color)
            	   ret = true;
            }
            
            return ret;   
        }        
        
        private function startMove(evt:MouseEvent):void{
            
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
        
        private function endMove(evt:DragEvent):void{
            
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
            
            if (evt.dragSource.hasFormat("token")){
                
                // define cell and destination
                var token:ManantialesToken = ManantialesToken (evt.dragSource.dataForFormat("token"));
                var boardCell:BoardCell = BoardCell(evt.currentTarget);
                var destination:Ficha = Ficha(token.ficha.clone());
                destination.row = boardCell.row;
                destination.column = boardCell.column;
                destination.type = token.ficha.type;
                
                // do move in backend
                var move:ManantialesMove = new ManantialesMove();
                move.player = _currentPlayer;
                move.destination = destination;
                move.status = Move.UNVERIFIED;
                var call:Object = _gameService.doMove(move);
                call.operation = GAME_SERVICE_DO_MOVE_OP;
                _executingMove = move;   
                              
                
                // do move in interface
                boardCell.reset();
                
                var newToken:ManantialesToken
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
                    var move:ManantialesMove = ManantialesMove(message.body);
                    addMove(move);
                    break;                    
                case GameEvent.PLAYER_CHANGE:
                    var players:ArrayCollection = ArrayCollection(message.body);
                    updatePlayers(players);
                    break;
                case GameEvent.CHECK_CONSTRAINT:
                    var checkConstraint:CheckConstraint = CheckConstraint(message.body);
                    handleCheckConstraint (checkConstraint);
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
                    _moves = ArrayCollection(event.result)
                    _moveViewer.initFromMoves(_moves);
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
            var ficha:Ficha;
            var token:ManantialesToken;
            _gameGrid = gameGrid;
            _board.clearTokens();
            
            /* Setup undeveloped tokens first */
            for (var col:int = 0; col < this._board.nCols; col++) {
                for (var row:int = 0; row < this._board.nRows; row++) {
                    var roundCell:RoundCell = RoundCell(_board.getBoardCell(col, row));
                    if (roundCell != null) {
                        var tok:UndevelopedToken = new UndevelopedToken(col, row);
                        roundCell.token = tok;
                    }
                }
            }              
            
            
            if (_gameGrid.cells && _gameGrid.cells.length > 0) {
                for (var i:Number = 0; i < _gameGrid.cells.length; i++){
                    ficha = Ficha(_gameGrid.cells[i]);
                    switch (ficha.type) {
                    	case TokenType.FOREST:
                    	   token = new ForestToken();
                    	   token.ficha = ficha;
                    	   if (token.ficha.color == this._currentPlayer.color)
                    	       this._forestStore.removeToken();
                    	   break;
                    	case TokenType.MODERATE:
                    	   token = new ModerateToken();
                    	   token.ficha = ficha;
                    	   if (token.ficha.color == this._currentPlayer.color)
                    	       this._moderateStore.removeToken();
                    	   break;
                        case TokenType.INTENSIVE:
                           token = new IntensiveToken();
                           token.ficha = ficha;
                           if (token.ficha.color == this._currentPlayer.color)
                                this._intensiveStore.removeToken();
                           break;
                        case TokenType.VIVERO:
                           token = new ViveroToken();
                           token.ficha = ficha;
                           if (token.ficha.color == this._currentPlayer.color)
                                this._viveroStore.removeToken();
                           break;
                        case TokenType.SILVOPASTORAL:
                           token = new SilvopastoralToken();
                           token.ficha = ficha;
                           if (token.ficha.color == this._currentPlayer.color)
                                this._silvoStore.removeToken();
                           break;
                        default:
                            break;
                    }                                      
                    _board.addToken(token);
                }
            }
        } 
        
        private function initTurn():void{
        	_moveViewer.enabled = false;
        	
            for (var i:int = 0; i < this._tokenStorePanels.length; i++) {
            	var obj:Object = _tokenStorePanels.getItemAt(i);
            	if (obj is Panel) {
                    var pnl:Panel = Panel(obj);
                    if (!pnl.visible)
                        pnl.visible = true;
                }
            }
        	
            _forestStore.active = true;
            _moderateStore.active = true;
            _intensiveStore.active = true;
            _viveroStore.active = true;
            _silvoStore.active = true;  
        }
        
        private function endTurn():void{
        	_moveViewer.enabled = true;
        	           
            for (var i:int = 0; i < this._tokenStorePanels.length; i++) {
            	var obj:Object = _tokenStorePanels.getItemAt(i);
                if (obj is Panel) {
                    var pnl:Panel = Panel(obj);            	
	                if (pnl.visible)
	                    pnl.visible = false;
	            }
            } 
            
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
            var gamePlayer:ManantialesPlayer;
            for (var i:int = 0; i < players.length; i++){
                gamePlayer = ManantialesPlayer(players[i]); 
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
        
        public function quitGame (gamePlayer:GamePlayer):void {
        	var call:Object = _gameService.quitGame(gamePlayer);
            call.operation = "quitGame";
            end();
        }
        
        private function addMove(move:ManantialesMove):void{
            
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
                _moveViewer.addMove(move);
                
                //if current move is the last move then animate
                if (_selectedMoveInd == _moves.length - 2){
                    _selectedMoveInd ++;
                    doMove(move);
                }
            }else{
                
                // Search for move in reverse order because its most likely to be the last move
                var oldMove:ManantialesMove;
                for (var i:Number = _moves.length - 1; i >= 0; i--){
                    oldMove = ManantialesMove(_moves[i]);
                    if (oldMove.id == move.id){
                        _moves[i] = move;
                        _moveViewer.updateMove(move);
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
        private function doMove(move:ManantialesMove):void{
            
            //check that destination is free
            var boardCell:RoundCell = RoundCell(_board.getBoardCell(
                move.destination.column, move.destination.row));
            if (!boardCell.token is UndevelopedToken || 
                boardCell.token is IntensiveToken) 
            {
                _moveViewer.selectedMove = move;
                return;
            }
            
            var destination:Ficha = Ficha(move.destination);                                      
            
            //define origin
            var startPoint:Point;
            var startSize:Number;

            if(move.player.id == _currentPlayer.id && _isTurn){
                switch (destination.type) {
                	case TokenType.FOREST:
	                   startPoint = new Point(_forestStore.width, _forestStore.height);
                       startPoint = _forestStore.localToGlobal(startPoint);
                       break;
                    case TokenType.INTENSIVE:
                        startPoint = new Point(_intensiveStore.width, _intensiveStore.height);
                        startPoint = _intensiveStore.localToGlobal(startPoint);
                	    break;
                	case TokenType.MODERATE:
                        startPoint = new Point(_moderateStore.width, _moderateStore.height);
                        startPoint = _moderateStore.localToGlobal(startPoint);
                        break;   
                    case TokenType.SILVOPASTORAL:
                        startPoint = new Point(_silvoStore.width, _intensiveStore.height);
                        startPoint = _silvoStore.localToGlobal(startPoint);
                        break;
                    case TokenType.VIVERO:
                        startPoint = new Point(_viveroStore.width, _intensiveStore.height);
                        startPoint = _viveroStore.localToGlobal(startPoint);
                        break;
                   default:
                        break;                                                         	   
                }	
            	            
                startPoint = _animateLayer.globalToLocal(startPoint);
                startSize = _board.tokenSize;
                
            } else{
                var playerBtn:Button = _playersViewer.getPlayerButton(
                    ManantialesPlayer(move.player));
                startPoint = new Point(
                    playerBtn.x + Color.getCellIconSize() / 2 + 5, 
                    playerBtn.y + Color.getCellIconSize() / 2 + 5);
                startPoint = _playersViewer.localToGlobal(startPoint);
                startPoint = _animateLayer.globalToLocal(startPoint);
                startSize = Color.getCellIconSize();
            }
            
            //define destination
            var endPoint:Point = new Point(boardCell.width / 2, boardCell.height / 2);
            var endSize:Number = _board.tokenSize;
            endPoint = boardCell.localToGlobal(endPoint);
            endPoint = _animateLayer.globalToLocal(endPoint);
            
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
            
            var token:ManantialesToken = ManantialesToken(AnimateProperty(event.currentTarget).target);
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
            
            _moveViewer.selectedMove = move;            
            
            boardCell.token = token;
            boardCell.token.blink(1);
            
            // Update move viewer
            var move:ManantialesMove = ManantialesMove(_moves[_selectedMoveInd])            
        }
        
        private function undoMove(move:ManantialesMove):void{
            
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
            var token:ManantialesToken = new ManantialesToken();
            token.cell = move.destination;
            token.width = endSize;
            token.height = endSize;
            boardCell.token = _previousToken;
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
            
            var token:ManantialesToken = ManantialesToken(AnimateProperty(event.currentTarget).target);
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
            
            _moveViewer.selectedMove = ManantialesMove(_moves[_selectedMoveInd]);
        } 
        
        private function handleCheckConstraint (checkConstraint:CheckConstraint):void {
        	Alert.show(checkConstraint.reason);
        } 
              
        
        public function destroy():void{
            _msgReceiver.destroy();
        }

	}
}