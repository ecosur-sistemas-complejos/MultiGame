package mx.ecosur.multigame.pente{
	
	import flash.events.MouseEvent;
	import flash.geom.Point;
	
	import mx.collections.ArrayCollection;
	import mx.controls.Alert;
	import mx.controls.Button;
	import mx.core.DragSource;
	import mx.core.IFlexDisplayObject;
	import mx.core.UIComponent;
	import mx.ecosur.multigame.component.BoardCell;
	import mx.ecosur.multigame.component.ChatPanel;
	import mx.ecosur.multigame.component.GameStatus;
	import mx.ecosur.multigame.component.Token;
	import mx.ecosur.multigame.component.TokenStore;
	import mx.ecosur.multigame.entity.Cell;
	import mx.ecosur.multigame.entity.ChatMessage;
	import mx.ecosur.multigame.entity.GameGrid;
	import mx.ecosur.multigame.entity.GamePlayer;
	import mx.ecosur.multigame.entity.Move;
	import mx.ecosur.multigame.event.GameEvent;
	import mx.ecosur.multigame.exception.ExceptionType;
	import mx.ecosur.multigame.helper.Color;
	import mx.ecosur.multigame.pente.entity.PenteGame;
	import mx.ecosur.multigame.pente.entity.PenteMove;
	import mx.effects.AnimateProperty;
	import mx.events.CloseEvent;
	import mx.events.DragEvent;
	import mx.events.EffectEvent;
	import mx.managers.DragManager;
	import mx.messaging.Consumer;
	import mx.messaging.events.MessageEvent;
	import mx.messaging.events.MessageFaultEvent;
	import mx.messaging.messages.ErrorMessage;
	import mx.messaging.messages.IMessage;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.remoting.RemoteObject;

	/**
	 * Represents a game of pente. Contains a pente board and cell store 
	 * and controls the main flow of control for the game.
	 */
	public class PenteGameController {
		
		// Visual components
		private var _board:PenteBoard;
		private var _chatPanel:ChatPanel;
		private var _playersViewer:PentePlayersViewer;
		private var _tokenStore:TokenStore;
		private var _gameStatus:GameStatus;
		private var _animateLayer:UIComponent;
		
		// Data objects
		private var _currentPlayer:GamePlayer;
		private var _players:ArrayCollection;
		private var _gameGrid:GameGrid;
		private var _game:PenteGame;
		private var _moves:Array;
		
		// Server objects
		private var _gameService:RemoteObject;
		private var _consumer:Consumer;
		
		// flags
		private var _isMoving:Boolean;
		private var _isTurn:Boolean;
		private var _isBoardEmtpy:Boolean;
		
		// constants
		private static const TOKEN_STORE_MIN_WIDTH:int = 150;
		private static const TOKEN_STORE_MAX_WIDTH:int = 300;
		private static const N_TOKENS_IN_STORE:int = 50;
		private static const MESSAGING_DESTINATION_NAME:String = "multigame-destination";
		private static const GAME_SERVICE_DESTINATION_NAME:String = "gameService";
		private static const GAME_SERVICE_GET_GRID_OP:String = "getGameGrid";
		private static const GAME_SERVICE_GET_PLAYERS_OP:String = "getPlayers";
		
		/**
		 * Default constructor. 
		 * 
		 */
		public function PenteGameController(currentPlayer:GamePlayer, board:PenteBoard, chatPanel:ChatPanel, playersViewer:PentePlayersViewer, tokenStore:TokenStore, gameStatus:GameStatus, animateLayer:UIComponent){
			super();
			
			//set private references
			_currentPlayer = currentPlayer;
			_board = board;
			_chatPanel = chatPanel;
			_tokenStore = tokenStore;
			_gameStatus = gameStatus;
			_playersViewer = playersViewer;
			_animateLayer = animateLayer;
			_moves = new Array();
			_isMoving = false;	
			
			//initialize game service remote object
			_gameService = new RemoteObject();
			_gameService.destination = GAME_SERVICE_DESTINATION_NAME;
			_gameService.addEventListener(ResultEvent.RESULT, gameServiceResultHandler);
			_gameService.addEventListener(FaultEvent.FAULT, gameServiceFaultHandler);
			
			//initialize consumer
			_consumer = new Consumer();
			_consumer.destination = MESSAGING_DESTINATION_NAME;
			_consumer.addEventListener(MessageEvent.MESSAGE, consumerMessageHandler);
			_consumer.addEventListener(MessageFaultEvent.FAULT, consumerFaultHandler);
			_consumer.subscribe();
			
			//initialize the board
			_board.dragEnterHandler = dragEnterBoardCell;
			_board.dragDropHandler = dragDropCell;
			_board.dragExitHandler = dragExitCell;	
			
			//initialize token store
			_tokenStore.startMoveHandler = startMove;
			_tokenStore.endMoveHandler = endMove;
			_tokenStore.active = false;
			
			//initialize game status
			_gameStatus.showMessage("Welcome to the game!", Color.getColorCode(currentPlayer.color));
			
			//get the game grid and players
			var callGrid:Object = _gameService.getGameGrid();
			callGrid.operation = GAME_SERVICE_GET_GRID_OP;
			var callPlayers:Object = _gameService.getPlayers();
			callPlayers.operation = GAME_SERVICE_GET_PLAYERS_OP;
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
					if (_isTurn){
						_gameStatus.showMessage("Its your turn", Color.getColorCode(_currentPlayer.color));
					}else{
						_gameStatus.showMessage(gamePlayer.player.name + " to move", Color.getColorCode(gamePlayer.color));
					}
				}
			}
			_playersViewer.players = players;
			_players = players;
		}
		
		/*
		 * Called when game begins 
		 */
		private function begin():void{
			
			//TODO: Do something here, manage the update player event aswell as the game begins event.
			_gameStatus.showMessage("All players have joined. The game will now begin.", 0x00000);	
		}
		
		/*
		 * Called when game ends 
		 */
		public function end():void{
			
			//TODO: Do something here depending on the winner etc.
			_gameStatus.showMessage("The game has finished.", 0x00000);	
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
						undoLastMove();
					}
					Alert.show("Sorry but this move is not valid", "Woops!", Alert.OK, null, fnc);
				}
			}else{
				Alert.show(event.fault.faultString, "Server Error");
			}
		}
		
		/*
		 * Consumer message handler. All messages contain a game event
		 * header, based on this different actions are taken.
		 */
		private function consumerMessageHandler(event:MessageEvent):void {
			var message:IMessage = event.message;
			var gameId:Number = message.headers.GAME_ID;
			var gameEvent:String = message.headers.GAME_EVENT;
				
			//Alert.show("game event received " + gameEvent);
		
			switch (gameEvent){
				case GameEvent.BEGIN:
					begin();
					break;
				case GameEvent.CHAT:
					var chatMessage:ChatMessage = ChatMessage(message.body); 
					_chatPanel.addMessage(chatMessage);
					if(chatMessage.sender.id != _currentPlayer.player.id){
						_gameStatus.showMessage(chatMessage.sender.name + " has sent you a message", 0x000000);
					}
					break;
				case GameEvent.END:
					end();
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
			
		private function consumerFaultHandler(event:MessageFaultEvent):void {
			Alert.show(event.faultString, "Error receiving message");
		}
		
		/*
		 * Adds a move to the internal list of moves. If the move is not present on the board then
		 * it is animated.  
		 */
		private function addMove(move:Move):void{
			if (_moves.length > 0){
				var lastMove:Move = _moves[_moves.length - 1]
				if (move.destination.row == lastMove.destination.row && move.destination.column == lastMove.destination.column){
					/* move is already shown, this client must have done the move */
					return;
				} 
			}
			_isBoardEmtpy = false;
			animateMove(move);
			_moves.push(move);
		}
		
		/*
		 * Animates a move
		 */
		private function animateMove(move:Move):void{
			
			//get board cell destination for move
			var boardCell:BoardCell = _board.getBoardCell(move.destination.column, move.destination.row);
			var cellPadding:Number = boardCell.getStyle("padding");
			var token:Token = new Token();
			token.cell = move.destination;
			token.width = _board.tokenSize;
			token.height = _board.tokenSize;
			_animateLayer.addChild(token);
			
			//get position of bottom right of store relative to the cell
			var playerBtn:Button = _playersViewer.getPlayerButton(move.player);
			var startPoint:Point = new Point(playerBtn.x + token.width / 2, playerBtn.y + (playerBtn.height - token.height) / 2);
			startPoint = _playersViewer.localToGlobal(startPoint);
			startPoint = _animateLayer.globalToLocal(startPoint);
			var endPoint:Point = new Point(boardCell.width / 2, boardCell.height / 2);
			endPoint = boardCell.localToGlobal(endPoint);
			endPoint = _animateLayer.globalToLocal(endPoint);
			
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
			apY.addEventListener(EffectEvent.EFFECT_END, endAnimateMove);
			
			//start effect
			apX.play();
			apY.play();
		}
		
		private function endAnimateMove(event:EffectEvent):void{
			
			var token:Token = Token(AnimateProperty(event.currentTarget).target);
			var boardCell:BoardCell = _board.getBoardCell(token.cell.column, token.cell.row);
			_animateLayer.removeChild(token);
			boardCell.token = token;
		}
		
		private function getGrid():void{
			var call:Object = _gameService.getGameGrid();
			call.operation = "getGameGrid";
		}
		
		private function initGrid(gameGrid:GameGrid):void{
			var cell:Cell;
			var token:Token;
			_gameGrid = gameGrid;
			_board.clearTokens();
			if (_gameGrid.cells && _gameGrid.cells.length > 0){
				_isBoardEmtpy = false;
				for (var i:Number = 0; i < _gameGrid.cells.length; i++){
					cell = Cell(_gameGrid.cells[i]);
					token = new Token();
					token.cell = cell;
					_board.addToken(token);
				}
			}else{
				_isBoardEmtpy = true;
			}
		} 
		
		private function initTurn():void{
			_tokenStore.active = true;
		}
		
		private function endTurn():void{
			_tokenStore.active = false;
		}
		
		private function startMove(evt:MouseEvent):void{
			
			if (!_isMoving && _isTurn){
			
				//initialize drag source
	            var token:Token = Token(evt.currentTarget);
	            var ds:DragSource = new DragSource();
	            ds.addData(token, "token");
	            	            
	            //create proxy image and start drag
	            var dragImage:IFlexDisplayObject = token.createDragImage();
	            DragManager.doDrag(token, ds, evt, dragImage);
            	_isMoving = true;
            	
   			}
		} 
		
		private function dragEnterBoardCell(evt:DragEvent):void{
			
			if (evt.dragSource.hasFormat("token")){
				
				var token:Token = Token(evt.dragSource.dataForFormat("token"));
				var boardCell:BoardCell = BoardCell(evt.currentTarget);
				boardCell.addEventListener(DragEvent.DRAG_EXIT, dragExitCell);
				
				//calculate if move is valid
				if (validateMove(boardCell)){
					boardCell.select(token.cell.colorCode);
					DragManager.acceptDragDrop(boardCell);
				}
			}
		}  
		
		private function dragDropCell(evt:DragEvent):void{
			
			if (evt.dragSource.hasFormat("token")){
				
				// define cell and destination
				var token:Token = Token (evt.dragSource.dataForFormat("token"));
				var boardCell:BoardCell = BoardCell(evt.currentTarget);
				var destination:Cell = token.cell.clone();
				destination.row = boardCell.row;
				destination.column = boardCell.column;
				
				// do move in backend
				var move:PenteMove = new PenteMove();
				move.player = _currentPlayer;
				move.destination = destination;
				move.status = Move.UNVERIFIED;
            	var call:Object = _gameService.doMove(move);
            	call.operation = "doMove";
				
				// do move in interface
				_moves.push(move);
				boardCell.reset();
				//_tokenStore.removeChild(token);
				var newToken:Token = new Token();
				newToken.cell = destination;
				_board.addToken(newToken);
			}
		}
		
		private function dragExitCell(evt:DragEvent):void{
			
			// unselect board cell
			if (evt.dragSource.hasFormat("token")){
				var boardCell:BoardCell = BoardCell(evt.currentTarget);
				boardCell.reset();
			}
		} 
		
		private function endMove(evt:DragEvent):void{
			
			if (evt.dragSource.hasFormat("token")){
				_isMoving = false;
				var token:Token = Token(evt.currentTarget);
				token.selected = false;
			}
		}
		
		private function highlightToken(evt:MouseEvent):void{
			
			if (!_isMoving && _isTurn){
				var token:Token = Token(evt.currentTarget);
				token.useHandCursor = true;
				token.selected = true;;
			}
		}
		
		private function restoreToken(evt:MouseEvent):void{
			
			if (!_isMoving && _isTurn){
				var token:Token = Token(evt.currentTarget);
				token.useHandCursor = false;
				token.selected = false;
			}
		}
		
		private function validateMove(boardCell:BoardCell):Boolean{
			
			if(_isBoardEmtpy){
				if(boardCell.row == Math.floor(_board.nRows / 2) && boardCell.column == Math.floor(_board.nCols / 2)){
					return true;
				}else{
					return false;
				}
			}else if (boardCell.token == null){
				return true;
			}
			return false;	
		}
		
		private function undoLastMove():void{
			/*
			// get move
			var move:Move = _moves.pop();
			var boardCell:BoardCell = _board.getBoardCell(move.destination.column, move.destination.row);
			
			// get position of bottom right of store relative to the cell
			var pt:Point = new Point((_tokenStore.x + _tokenStore.width), (_tokenStore.y + _tokenStore.height));
			pt = localToGlobal(pt);
			pt = boardCell.globalToLocal(pt);
			
			// define motion animation
			var token:Token = boardCell.token;
			var apX:AnimateProperty = new AnimateProperty(token);
			apX.toValue = pt.x;
			apX.duration = 1000;
			apX.property = "x";
			var apY:AnimateProperty = new AnimateProperty(token);
			apY.toValue = pt.y;
			apY.duration = 1000;
			apY.property = "y";
			
			// define handler for effect end
			var endEffect:Function = function(event:EffectEvent):void{
				
				// add token to store and remove token from board
				addStoreToken();
				boardCell.token = null;
			
				// force update of display list
				invalidateDisplayList();
			}
			apX.addEventListener(EffectEvent.EFFECT_END, endEffect);
			
			// start effect
			apX.play();
			apY.play();
			*/
		}
		/*
		private function addStoreToken():void{
			
			var token:Token = new Token();
			token.alpha = Token.DISACTIVATED_ALPHA;
			token.buttonMode = false;
			token.addEventListener(MouseEvent.MOUSE_OVER, highlightToken);
			token.addEventListener(MouseEvent.MOUSE_OUT, restoreToken);
			token.addEventListener(MouseEvent.MOUSE_DOWN, startMove);
			token.addEventListener(DragEvent.DRAG_COMPLETE, endMove);
			_tokenStore.addChild(token);	
		}
*/		
		/* Overrides */ 
		/*
		override protected function createChildren():void {
			
			//create board 
			_board = new PenteBoard(19, 19, dragEnterBoardCell, dragDropCell, dragExitCell);
			addChild(_board);
			
			//create token store
			_tokenStore = new Canvas();
			_tokenStore.horizontalScrollPolicy = ScrollPolicy.OFF;
			_tokenStore.verticalScrollPolicy = ScrollPolicy.OFF;
			_tokenStore.setStyle("backgroundColor", 0x666666);
			_tokenStore.setStyle("borderColor", 0xffffff);
			_tokenStore.setStyle("borderThickness", 5);
			_tokenStore.setStyle("borderStyle", "inset");
			addChild(_tokenStore);
			
			//create tokens and add to store
			for (var i:int = 0; i < N_TOKENS_IN_STORE; i++){
				addStoreToken();		
			}
			_tokenStore.alpha = 0.6;
		}
		*/
		/*
		override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void{
			
			//inforce minimum width and height: TODO - This allows other components to overlap - look for solution
			//unscaledWidth = Math.max(unscaledWidth, 300);
			//unscaledHeight = Math.max(unscaledHeight, 300);
			
			
			//redefine cell size of board and position in center of game
			var boardWidth:Number = unscaledWidth - TOKEN_STORE_MIN_WIDTH - 10;
			var tokenStoreWidth:Number;
			if (boardWidth / _board.nCols >= unscaledHeight / _board.nRows){
				
				//board limited by height, calculate maximum cell size
				_board.boardCellSize = unscaledHeight / _board.nRows;
				
				//calculate cellStore width and position board
				boardWidth = _board.boardCellSize * _board.nCols
				tokenStoreWidth = Math.min(TOKEN_STORE_MAX_WIDTH, unscaledWidth - boardWidth - 10);
				_board.x = tokenStoreWidth + (unscaledWidth - tokenStoreWidth - boardWidth) / 2;
				
			}else{
				
				//board limited by height, calculate maximum cell size
				_board.boardCellSize = boardWidth / _board.nCols;
				
				//calculate cellStore width and position board
				boardWidth = _board.boardCellSize * _board.nCols
				tokenStoreWidth = TOKEN_STORE_MIN_WIDTH + 10;
				_board.x = tokenStoreWidth;
			}
			
			//draw cell store
			var token:Token; 
			var tokenW:Number = _board.boardCellSize - _board.cellPadding;
			var tokenH:Number =   _board.boardCellSize - _board.cellPadding;
			var tokensPerRow:int = Math.floor((tokenStoreWidth - (tokenW * 0.4)) / (tokenW * 0.6));
			var paddingLeft:Number = (tokenStoreWidth - (tokensPerRow * tokenW * 0.6) - (tokenW * 0.4)) / 2; 
			_tokenStore.width = tokenStoreWidth;
			_tokenStore.height = Math.ceil(_tokenStore.getChildren().length / tokensPerRow) * (tokenH * 0.6) + (tokenH * 0.4) + 10;
			_tokenStore.setStyle("borderColor", Color.getColorCode(_currentPlayer.color));
			for(var i:int = 0; i < _tokenStore.getChildren().length; i++){
				
				token = Token(_tokenStore.getChildAt(i));
				token.width = tokenW;
				token.height = tokenH;
				token.x = paddingLeft + (i % tokensPerRow) * tokenW * 0.6 + tokenW / 2;
				token.y = 5 + Math.floor(i / tokensPerRow) * tokenH * 0.6 + tokenH / 2;
				token.cell = new Cell();
				token.cell.color = _currentPlayer.color;
			}
		}
		*/
	}
}