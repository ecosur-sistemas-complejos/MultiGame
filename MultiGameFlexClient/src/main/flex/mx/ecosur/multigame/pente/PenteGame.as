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
	import mx.ecosur.multigame.Color;
	import mx.ecosur.multigame.entity.BoardCell;
	import mx.ecosur.multigame.entity.Cell;
	import mx.ecosur.multigame.entity.GameGrid;
	import mx.ecosur.multigame.entity.GamePlayer;
	import mx.ecosur.multigame.entity.Move;
	import mx.ecosur.multigame.exception.ExceptionType;
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
	
	//define player change event
	[Event(name = "playerChange")]

	public class PenteGame extends UIComponent{
		
		private var _currentPlayer:GamePlayer;
		private var _players:ArrayCollection;
		private var _gameGrid:GameGrid;
		private var _gameId:int;
		private var _gameService:RemoteObject;
		private var _board:PenteBoard;
		private var _cellStore:Canvas;
		private var _moves:Array;
		
		//flags
		private var _isMoving:Boolean;
		private var _isTurn:Boolean;
		private var _isBoardEmtpy:Boolean;
		
		//constants
		private static const CELL_STORE_MIN_WIDTH:int = 150;
		private static const CELL_STORE_MAX_WIDTH:int = 300;
		private static const N_CELLS_IN_STORE:int = 50;
		
		public function PenteGame(){
			super();
			
			_isMoving = false;
			
			_gameService = new RemoteObject();
			_gameService.destination = "gameService";
			_gameService.addEventListener(ResultEvent.RESULT, resultHandler);
			_gameService.addEventListener(FaultEvent.FAULT, faultHandler);
			
			_moves = new Array();
			
			addEventListener(FlexEvent.CREATION_COMPLETE, function(event:FlexEvent):void{getGrid()});
		}
		
		/*
		 * Getters and setters
		 */
		public function set currentPlayer(currentPlayer:GamePlayer):void{
			_currentPlayer = currentPlayer;
			//Alert.show(_currentPlayer.game.id.toString());
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
		
		public function set gameId(gameId:int):void{
			_gameId = gameId;
		}  
		
		/* 
		 * Remote object handlers
		 */
		private function resultHandler(event:ResultEvent):void{
			var call:Object = event.token;
			//Alert.show("result received " + call.operation);
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
		
		public function begin():void{
			getGrid();
		}
		
		/*
		public function turn():void{
			getGrid();
			var call:Object = _gameService.getPlayers();
			call.operation = "getPlayers";
		}*/
		
		/*
		public function playerChange():void{
			var call:Object = _gameService.getPlayers();
			call.operation = "getPlayers";
		}*/
		
		public function addMove(move:Move):void{
			if (_moves.length > 0){
				var lastMove:Move = _moves[_moves.length - 1]
				if (move.destination.row == lastMove.destination.row && move.destination.column == lastMove.destination.column){
					//move is already shown, this client must have done the move
					return;
				} 
			}
			_isBoardEmtpy = false;
			animateMove(move);
			_moves.push(move);
		}
		
		public function animateMove(move:Move):void{
			
			//get board cell destination for move
			var cell:Cell = move.destination;
			var boardCell:BoardCell = _board.getBoardCell(cell.column, cell.row);
			var cellPadding:Number = boardCell.getStyle("padding");
			cell.width = boardCell.width - 2 * cellPadding;
			cell.height = boardCell.height - 2 * cellPadding;
			boardCell.cell = cell;
			
			//get position of bottom right of store relative to the cell
			var pt:Point = new Point((_cellStore.x + _cellStore.width), (_cellStore.y + _cellStore.height));
			pt = localToGlobal(pt);
			pt = boardCell.globalToLocal(pt);
			
			//define motion animation
			var apX:AnimateProperty = new AnimateProperty(cell);
			apX.fromValue = pt.x;
			apX.toValue = boardCell.width / 2;
			apX.duration = 1000;
			apX.property = "x";
			var apY:AnimateProperty = new AnimateProperty(cell);
			apY.fromValue = pt.y;
			apY.toValue = boardCell.height / 2;
			apY.duration = 1000;
			apY.property = "y";
			
			//start effect
			apX.play();
			apY.play();
		}
		
		public function end():void{
			Alert.show("Game ended");	
		}
		
		private function getGrid():void{
			var call:Object = _gameService.getGameGrid();
			call.operation = "getGameGrid";
		}
		
		private function initGrid(gameGrid:GameGrid):void{
			var cell:Cell;
			_gameGrid = gameGrid;
			_board.clearCells();
			if (_gameGrid.cells && _gameGrid.cells.length > 0){
				_isBoardEmtpy = false;
				for (var i:Number = 0; i < _gameGrid.cells.length; i++){
					cell = Cell(_gameGrid.cells[i]);
					_board.addCell(cell);
				}
			}else{
				_isBoardEmtpy = true;
			}
			initTurn();
		} 
		
		private function initTurn():void{
			var cell:Cell;
			if (_gameGrid.cells){
				for (var i:Number = 0; i < _gameGrid.cells.length; i++){
					cell = Cell(_gameGrid.cells[i]);
					_board.addCell(cell);
				}
			}
			if (!_isTurn){
				_cellStore.alpha = 0.6;
				for(var j:int = 0; j < _cellStore.getChildren().length; j++){
					Cell(_cellStore.getChildAt(j)).buttonMode = false;
				}
			}else{
				_cellStore.alpha = 1;
				for(var k:int = 0; k < _cellStore.getChildren().length; k++){
					Cell(_cellStore.getChildAt(k)).buttonMode = true;
				}
			}
		}
		
		private function startMove(evt:MouseEvent):void{
			
			if (!_isMoving && _isTurn){
			
				//select the cell
	            var cell:Cell = Cell(evt.currentTarget);
				
				//initialize drag source
	            var ds:DragSource = new DragSource();
	            ds.addData(cell, "cell");
	            	            
	            //create proxy image and start drag
	            var dragImage:IFlexDisplayObject = cell.createDragImage();
	            DragManager.doDrag(cell, ds, evt, dragImage);
            	_isMoving = true;
            	
   			}
		} 
		
		public function dragEnterBoardCell(evt:DragEvent):void{
			if (evt.dragSource.hasFormat("cell")){
				var cell:Cell = Cell (evt.dragSource.dataForFormat("cell"));
				var boardCell:BoardCell = BoardCell(evt.currentTarget);
				boardCell.addEventListener(DragEvent.DRAG_EXIT, dragExitCell);
				if (validateMove(boardCell)){
					boardCell.select(cell.colorCode);
					DragManager.acceptDragDrop(boardCell);
				}
			}
		}  
		
		public function dragDropCell(evt:DragEvent):void{
			if (evt.dragSource.hasFormat("cell")){
				
				//define cell and destination
				var cell:Cell = Cell (evt.dragSource.dataForFormat("cell"));
				var boardCell:BoardCell = BoardCell(evt.currentTarget);
				var destination:Cell = cell.clone();
				destination.row = boardCell.row;
				destination.column = boardCell.column;
				
				//do move in backend
				var move:PenteMove = new PenteMove();
				move.player = _currentPlayer;
				move.destination = destination;
				move.status = Move.UNVERIFIED;
            	var call:Object = _gameService.doMove(move);
            	call.operation = "doMove";
				
				//do move in interface
				_moves.push(move);
				boardCell.reset();
				_cellStore.removeChild(cell);
				_board.addCell(destination);
			}
		}
		
		public function dragExitCell(evt:DragEvent):void{
			if (evt.dragSource.hasFormat("cell")){
				var boardCell:BoardCell = BoardCell(evt.currentTarget);
				boardCell.reset();
			}
		} 
		
		private function endMove(evt:DragEvent):void{
			if (evt.dragSource.hasFormat("cell")){
				_isMoving = false;
				var cell:Cell = Cell(evt.currentTarget);
				cell.alpha = Cell.DISACTIVATED_ALPHA;
			}
		}
		
		private function highlightCell(evt:MouseEvent):void{
			if (!_isMoving && _isTurn){
				var cell:Cell = Cell(evt.currentTarget);
				cell.useHandCursor = true;
				cell.alpha = Cell.HIGHLIGHT_ALPHA;
			}
		}
		
		private function restoreCell(evt:MouseEvent):void{
			if (!_isMoving && _isTurn){
				evt.currentTarget.useHandCursor = false;
				evt.currentTarget.alpha = Cell.DISACTIVATED_ALPHA;
			}
		}
		
		private function validateMove(boardCell:BoardCell):Boolean{
			if(_isBoardEmtpy){
				if(boardCell.row == Math.floor(_board.nRows / 2) && boardCell.column == Math.floor(_board.nCols / 2)){
					return true;
				}else{
					return false;
				}
			}else if (boardCell.cell == null){
				return true;
			}
			return false;	
		}
		
		private function undoLastMove():void{
			
			//get move
			var move:Move = _moves.pop();
			var boardCell:BoardCell = _board.getBoardCell(move.destination.column, move.destination.row);
			
			//get position of bottom right of store relative to the cell
			var cell:Cell = boardCell.cell;
			var pt:Point = new Point((_cellStore.x + _cellStore.width), (_cellStore.y + _cellStore.height));
			pt = localToGlobal(pt);
			pt = boardCell.globalToLocal(pt);
			
			//define motion animation
			var apX:AnimateProperty = new AnimateProperty(cell);
			apX.toValue = pt.x;
			apX.duration = 1000;
			apX.property = "x";
			var apY:AnimateProperty = new AnimateProperty(cell);
			apY.toValue = pt.y;
			apY.duration = 1000;
			apY.property = "y";
			
			//define handler for effect end
			var endEffect:Function = function(event:EffectEvent):void{
				
				//add cell to store and remove cell from board
				addStoreCell();
				boardCell.cell = null;
			
				//force update of display list
				invalidateDisplayList();
			}
			apX.addEventListener(EffectEvent.EFFECT_END, endEffect);
			
			//start effect
			apX.play();
			apY.play();
		}
		
		//-----------------------------------------------------------------------------------
		// Component override methods
		//-----------------------------------------------------------------------------------
		
		override protected function createChildren():void {
			
			//create board
			_board = new PenteBoard(19, 19, dragEnterBoardCell, dragDropCell, dragExitCell);
			addChild(_board);
			
			//create cell store
			_cellStore = new Canvas();
			_cellStore.horizontalScrollPolicy = ScrollPolicy.OFF;
			_cellStore.verticalScrollPolicy = ScrollPolicy.OFF;
			_cellStore.setStyle("backgroundColor", 0x666666);
			_cellStore.setStyle("borderColor", 0xffffff);
			_cellStore.setStyle("borderThickness", 5);
			_cellStore.setStyle("borderStyle", "inset");
			addChild(_cellStore);
			
			//create cells and add to store
			for (var i:int = 0; i < N_CELLS_IN_STORE; i++){
				addStoreCell();		
			}
			_cellStore.alpha = 0.6;
		}
		
		private function addStoreCell():void{
			var cell:Cell = new Cell();
			cell.alpha = Cell.DISACTIVATED_ALPHA;
			cell.buttonMode = false;
			cell.addEventListener(MouseEvent.MOUSE_OVER, highlightCell);
			cell.addEventListener(MouseEvent.MOUSE_OUT, restoreCell);
			cell.addEventListener(MouseEvent.MOUSE_DOWN, startMove);
			cell.addEventListener(DragEvent.DRAG_COMPLETE, endMove);
			_cellStore.addChild(cell);	
		}
		
		override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void{
			
			//inforce minimum width and height: TODO - This allows other components to overlap - look for solution
			//unscaledWidth = Math.max(unscaledWidth, 300);
			//unscaledHeight = Math.max(unscaledHeight, 300);
			
			
			//redefine cell size of board and position in center of game
			var boardWidth:Number = unscaledWidth - CELL_STORE_MIN_WIDTH - 10;
			var cellStoreWidth:Number;
			if (boardWidth / _board.nCols >= unscaledHeight / _board.nRows){
				
				//board limited by height, calculate maximum cell size
				_board.boardCellSize = unscaledHeight / _board.nRows;
				
				//calculate cellStore width and position board
				boardWidth = _board.boardCellSize * _board.nCols
				cellStoreWidth = Math.min(CELL_STORE_MAX_WIDTH, unscaledWidth - boardWidth - 10);
				_board.x = cellStoreWidth + (unscaledWidth - cellStoreWidth - boardWidth) / 2;
				
			}else{
				
				//board limited by height, calculate maximum cell size
				_board.boardCellSize = boardWidth / _board.nCols;
				
				//calculate cellStore width and position board
				boardWidth = _board.boardCellSize * _board.nCols
				cellStoreWidth = CELL_STORE_MIN_WIDTH + 10;
				_board.x = cellStoreWidth;
			}
			
			//draw cell store
			var cell:Cell; 
			var cellW:Number = _board.boardCellSize - _board.cellPadding;
			var cellH:Number =   _board.boardCellSize - _board.cellPadding;
			var cellsPerRow:int = Math.floor((cellStoreWidth - (cellW * 0.4)) / (cellW * 0.6));
			var paddingLeft:Number = (cellStoreWidth - (cellsPerRow * cellW * 0.6) - (cellW * 0.4)) / 2; 
			_cellStore.width = cellStoreWidth;
			_cellStore.height = Math.ceil(_cellStore.getChildren().length / cellsPerRow) * (cellH * 0.6) + (cellH * 0.4) + 10;
			_cellStore.setStyle("borderColor", Color.getColorCode(_currentPlayer.color));
			for(var i:int = 0; i < _cellStore.getChildren().length; i++){
				
				cell = Cell(_cellStore.getChildAt(i));
				cell.width = cellW;
				cell.height = cellH;
				
				//random position
				//cell.x = cellW / 2 + Math.random() * (CELL_STORE_SIZE - cellW);
				//cell.y = cellH / 2 + Math.random() * (CELL_STORE_SIZE - cellH);
				
				//linear position
				cell.x = paddingLeft + (i % cellsPerRow) * cellW * 0.6 + cellW / 2;
				cell.y = 5 + Math.floor(i / cellsPerRow) * cellH * 0.6 + cellH / 2;
				
				cell.color = _currentPlayer.color;
			}
		}
	}
}