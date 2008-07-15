package mx.ecosur.multigame.pente{
	
	import flash.events.MouseEvent;
	import flash.geom.Point;
	
	import mx.collections.ArrayCollection;
	import mx.containers.Canvas;
	import mx.controls.Alert;
	import mx.core.DragSource;
	import mx.core.IFlexDisplayObject;
	import mx.core.ScrollPolicy;
	import mx.core.UIComponent;
	import mx.ecosur.multigame.component.BoardCell;
	import mx.ecosur.multigame.component.Token;
	import mx.ecosur.multigame.entity.Cell;
	import mx.ecosur.multigame.entity.GameGrid;
	import mx.ecosur.multigame.entity.GamePlayer;
	import mx.ecosur.multigame.entity.Move;
	import mx.ecosur.multigame.exception.ExceptionType;
	import mx.ecosur.multigame.helper.Color;
	import mx.ecosur.multigame.pente.entity.PenteMove;
	import mx.ecosur.multigame.pente.entity.PenteGame;
	import mx.effects.AnimateProperty;
	import mx.events.CloseEvent;
	import mx.events.DragEvent;
	import mx.events.EffectEvent;
	import mx.events.FlexEvent;
	import mx.managers.DragManager;
	import mx.messaging.messages.ErrorMessage;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.remoting.RemoteObject;

	/**
	 * Represents a game of pente. Contains a pente board and cell store 
	 * and controls the main flow of control for the game.
	 */
	public class PenteGameController extends UIComponent{
		
		private var _currentPlayer:GamePlayer;
		private var _players:ArrayCollection;
		private var _gameGrid:GameGrid;
		private var _game:PenteGame;
		private var _gameService:RemoteObject;
		private var _board:PenteBoard;
		private var _tokenStore:Canvas;
		private var _moves:Array;
		
		/* flags */
		private var _isMoving:Boolean;
		private var _isTurn:Boolean;
		private var _isBoardEmtpy:Boolean;
		
		/* constants */
		private static const TOKEN_STORE_MIN_WIDTH:int = 150;
		private static const TOKEN_STORE_MAX_WIDTH:int = 300;
		private static const N_TOKENS_IN_STORE:int = 50;
		
		/**
		 * Default constructor. 
		 * 
		 */
		public function PenteGameController(){
			super();
			
			_isMoving = false;
			
			_gameService = new RemoteObject();
			_gameService.destination = "gameService";
			_gameService.addEventListener(ResultEvent.RESULT, resultHandler);
			_gameService.addEventListener(FaultEvent.FAULT, faultHandler);
			
			_moves = new Array();
			
			addEventListener(FlexEvent.CREATION_COMPLETE, function(event:FlexEvent):void{getGrid()});
		}
		
		/* Getters and setters */
		
		public function set currentPlayer(currentPlayer:GamePlayer):void{
			_currentPlayer = currentPlayer;
			if (_isTurn != currentPlayer.turn){
				_isTurn = currentPlayer.turn;
				initTurn();
			}
		}
		
		public function get players():ArrayCollection{
			return _players;
		}
		
		[Bindable]
		public function set players(players:ArrayCollection):void{
			_players = players;
		}
		
		public function set game(game:PenteGame):void{
			_game = game;
		}  
		
		/* Remote object handlers */
		
		private function resultHandler(event:ResultEvent):void{
			var call:Object = event.token;
			switch (call.operation){
				case "getGameGrid":
					initGrid(GameGrid(event.result));
					break;
				case "getPlayers":
					updatePlayers(ArrayCollection(event.result));
					break;
			}
		}
		
		private function faultHandler(event:FaultEvent):void{
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
		
		/**
		 * Updates the list of players
		 *  
		 * @param players the new list of players
		 */
		public function updatePlayers(players:ArrayCollection):void{
			var gamePlayer:GamePlayer;
			for (var i:int = 0; i < players.length; i++){
				gamePlayer = GamePlayer(players[i]); 
				if (gamePlayer.id == _currentPlayer.id){
					this.currentPlayer = gamePlayer;
				}
			}
			this.players = players;
		}
				
		/**
		 * Adds a move to the internal list of moves. If the move is not present on the board then
		 * it is animated.
		 *  
		 * @param move the move to add
		 */
		public function addMove(move:Move):void{
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
		
		/**
		 * Called when game begins 
		 */
		public function begin():void{
			
			//TODO: Do something here depending on the winner etc.
			Alert.show("Game beginning");	
		}
		
		/**
		 * Called when game ends 
		 */
		public function end():void{
			
			//TODO: Do something here depending on the winner etc.
			Alert.show("Game ended");	
		}
		
		private function animateMove(move:Move):void{
			
			/* get board cell destination for move */
			var boardCell:BoardCell = _board.getBoardCell(move.destination.column, move.destination.row);
			var cellPadding:Number = boardCell.getStyle("padding");
			var token:Token = new Token();
			token.cell = move.destination;
			token.width = boardCell.width - 2 * cellPadding;
			token.height = boardCell.height - 2 * cellPadding;
			boardCell.token = token;
			
			/* get position of bottom right of store relative to the cell */
			var pt:Point = new Point((_tokenStore.x + _tokenStore.width), (_tokenStore.y + _tokenStore.height));
			pt = localToGlobal(pt);
			pt = boardCell.globalToLocal(pt);
			
			/* define motion animation */
			var apX:AnimateProperty = new AnimateProperty(token);
			apX.fromValue = pt.x;
			apX.toValue = boardCell.width / 2;
			apX.duration = 1000;
			apX.property = "x";
			var apY:AnimateProperty = new AnimateProperty(token);
			apY.fromValue = pt.y;
			apY.toValue = boardCell.height / 2;
			apY.duration = 1000;
			apY.property = "y";
			
			/* start effect */
			apX.play();
			apY.play();
		}
		
		private function getGrid():void{
			var call:Object = _gameService.getGameGrid();
			call.operation = "getGameGrid";
		}
		
		//TODO: Check whether this function is used
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
			initTurn();
		} 
		
		private function initTurn():void{
			//TODO: Look for better way to activate/disactivate cellStore
			if (!_isTurn){
				_tokenStore.alpha = 0.6;
				for(var j:int = 0; j < _tokenStore.getChildren().length; j++){
					Token(_tokenStore.getChildAt(j)).buttonMode = false;
				}
			}else{
				_tokenStore.alpha = 1;
				for(var k:int = 0; k < _tokenStore.getChildren().length; k++){
					Token(_tokenStore.getChildAt(k)).buttonMode = true;
				}
			}
		}
		
		private function startMove(evt:MouseEvent):void{
			
			if (!_isMoving && _isTurn){
			
				/* initialize drag source */
	            var token:Token = Token(evt.currentTarget);
	            var ds:DragSource = new DragSource();
	            ds.addData(token, "token");
	            	            
	            /* create proxy image and start drag */
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
				
				/* Calculate if move is valid */
				if (validateMove(boardCell)){
					boardCell.select(token.cell.colorCode);
					DragManager.acceptDragDrop(boardCell);
				}
			}
		}  
		
		private function dragDropCell(evt:DragEvent):void{
			
			if (evt.dragSource.hasFormat("token")){
				
				/* define cell and destination */
				var token:Token = Token (evt.dragSource.dataForFormat("token"));
				var boardCell:BoardCell = BoardCell(evt.currentTarget);
				var destination:Cell = token.cell.clone();
				destination.row = boardCell.row;
				destination.column = boardCell.column;
				
				/* do move in backend */
				var move:PenteMove = new PenteMove();
				move.player = _currentPlayer;
				move.destination = destination;
				move.status = Move.UNVERIFIED;
            	var call:Object = _gameService.doMove(move);
            	call.operation = "doMove";
				
				/* do move in interface */
				_moves.push(move);
				boardCell.reset();
				_tokenStore.removeChild(token);
				var newToken:Token = new Token();
				newToken.cell = destination;
				_board.addToken(newToken);
			}
		}
		
		private function dragExitCell(evt:DragEvent):void{
			
			/* unselect board cell */
			if (evt.dragSource.hasFormat("token")){
				var boardCell:BoardCell = BoardCell(evt.currentTarget);
				boardCell.reset();
			}
		} 
		
		private function endMove(evt:DragEvent):void{
			
			if (evt.dragSource.hasFormat("token")){
				_isMoving = false;
				var token:Token = Token(evt.currentTarget);
				token.alpha = Token.DISACTIVATED_ALPHA;
			}
		}
		
		private function highlightToken(evt:MouseEvent):void{
			
			if (!_isMoving && _isTurn){
				var token:Token = Token(evt.currentTarget);
				token.useHandCursor = true;
				token.alpha = Token.HIGHLIGHT_ALPHA;
			}
		}
		
		private function restoreToken(evt:MouseEvent):void{
			
			if (!_isMoving && _isTurn){
				evt.currentTarget.useHandCursor = false;
				evt.currentTarget.alpha = Token.DISACTIVATED_ALPHA;
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
			
			/* get move */
			var move:Move = _moves.pop();
			var boardCell:BoardCell = _board.getBoardCell(move.destination.column, move.destination.row);
			
			/* get position of bottom right of store relative to the cell */
			var pt:Point = new Point((_tokenStore.x + _tokenStore.width), (_tokenStore.y + _tokenStore.height));
			pt = localToGlobal(pt);
			pt = boardCell.globalToLocal(pt);
			
			/* define motion animation */
			var token:Token = boardCell.token;
			var apX:AnimateProperty = new AnimateProperty(token);
			apX.toValue = pt.x;
			apX.duration = 1000;
			apX.property = "x";
			var apY:AnimateProperty = new AnimateProperty(token);
			apY.toValue = pt.y;
			apY.duration = 1000;
			apY.property = "y";
			
			/* define handler for effect end */
			var endEffect:Function = function(event:EffectEvent):void{
				
				/* add token to store and remove token from board */
				addStoreToken();
				boardCell.token = null;
			
				/* force update of display list */
				invalidateDisplayList();
			}
			apX.addEventListener(EffectEvent.EFFECT_END, endEffect);
			
			/* start effect */
			apX.play();
			apY.play();
		}
		
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
		
		/* Overrides */ 
		
		override protected function createChildren():void {
			
			/* create board */ 
			_board = new PenteBoard(19, 19, dragEnterBoardCell, dragDropCell, dragExitCell);
			addChild(_board);
			
			/* create token store */
			_tokenStore = new Canvas();
			_tokenStore.horizontalScrollPolicy = ScrollPolicy.OFF;
			_tokenStore.verticalScrollPolicy = ScrollPolicy.OFF;
			_tokenStore.setStyle("backgroundColor", 0x666666);
			_tokenStore.setStyle("borderColor", 0xffffff);
			_tokenStore.setStyle("borderThickness", 5);
			_tokenStore.setStyle("borderStyle", "inset");
			addChild(_tokenStore);
			
			/* create tokens and add to store */
			for (var i:int = 0; i < N_TOKENS_IN_STORE; i++){
				addStoreToken();		
			}
			_tokenStore.alpha = 0.6;
		}
		
		override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void{
			
			//inforce minimum width and height: TODO - This allows other components to overlap - look for solution
			//unscaledWidth = Math.max(unscaledWidth, 300);
			//unscaledHeight = Math.max(unscaledHeight, 300);
			
			
			/* redefine cell size of board and position in center of game */
			var boardWidth:Number = unscaledWidth - TOKEN_STORE_MIN_WIDTH - 10;
			var tokenStoreWidth:Number;
			if (boardWidth / _board.nCols >= unscaledHeight / _board.nRows){
				
				/* board limited by height, calculate maximum cell size */
				_board.boardCellSize = unscaledHeight / _board.nRows;
				
				/* calculate cellStore width and position board */
				boardWidth = _board.boardCellSize * _board.nCols
				tokenStoreWidth = Math.min(TOKEN_STORE_MAX_WIDTH, unscaledWidth - boardWidth - 10);
				_board.x = tokenStoreWidth + (unscaledWidth - tokenStoreWidth - boardWidth) / 2;
				
			}else{
				
				/* board limited by height, calculate maximum cell size */
				_board.boardCellSize = boardWidth / _board.nCols;
				
				/* calculate cellStore width and position board */
				boardWidth = _board.boardCellSize * _board.nCols
				tokenStoreWidth = TOKEN_STORE_MIN_WIDTH + 10;
				_board.x = tokenStoreWidth;
			}
			
			/* draw cell store */
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
	}
}