package mx.ecosur.multigame.checkers{
	import flash.events.MouseEvent;
	
	import mx.controls.Alert;
	import mx.core.DragSource;
	import mx.core.IFlexDisplayObject;
	import mx.core.UIComponent;
	import mx.ecosur.multigame.entity.BoardCell;
	import mx.ecosur.multigame.entity.Cell;
	import mx.ecosur.multigame.entity.GameGrid;
	import mx.ecosur.multigame.entity.Move;
	import mx.ecosur.multigame.entity.Player;
	import mx.events.DragEvent;
	import mx.managers.DragManager;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.remoting.RemoteObject;

	public class CheckersGame extends UIComponent{
		
		private var _player:Player;
		private var _gameGrid:GameGrid;
		private var _gameId:int;
		private var _board:CheckersBoard;
		private var _validMoves:Object; //dictionary object for valid moves each key is cols#rows which corresponds to a cell and in its value there is a valid moves object with cols#rows for all possible move destinations.
		
		private var _gameService:RemoteObject
		
		//flags
		private var _isMoving:Boolean;
		
		public function CheckersGame(){
			super();
			
			_isMoving = false;
			_gameService = new RemoteObject();
			_gameService.destination = "gameService";
			_gameService.addEventListener(ResultEvent.RESULT, resultHandler);
			_gameService.addEventListener(FaultEvent.FAULT, faultHandler);
			
			//TODO: Reset valid moves on turn change
			_validMoves = new Object();
		}
		
		public function set player(player:Player):void{
			_player = player;
		}
		
		public function set gameId(gameId:int):void{
			if (gameId != _gameId){
				_gameId = gameId;
				var call:Object = _gameService.getGameGrid(_gameId);
				call.operation = "getGameGrid";
			}
		}  
		
		private function initGrid(gameGrid:GameGrid):void{
			var checker:Checker;
			_gameGrid = gameGrid;
			if (_gameGrid.cells){
				for (var i:Number = 0; i < _gameGrid.cells.length; i++){
					checker = Checker(_gameGrid.cells[i]);
					_board.addCell(checker);
					checker.addEventListener(MouseEvent.MOUSE_OVER, highlightCell);
					checker.addEventListener(MouseEvent.MOUSE_OUT, restoreCell);
					checker.addEventListener(MouseEvent.MOUSE_DOWN, startMove);
					checker.addEventListener(DragEvent.DRAG_COMPLETE, endMove);
				}
			}
		} 
		
		private function resultHandler(event:ResultEvent):void{
			var call:Object = event.token;
			switch (call.operation){
				case "getGameGrid":
					initGrid(GameGrid(event.result));
					break;
				case "getValidMoves":
					defineValidMoves(event.result, call.cell);
					break;
			}
		}
		
		private function faultHandler(event:FaultEvent):void{
			Alert.show(event.fault.faultString, "Error");
		}
		
		private function highlightCell(evt:MouseEvent):void{
			if (!_isMoving){
				evt.currentTarget.alpha = Cell.HIGHLIGHT_ALPHA;
			}
		}
		
		private function restoreCell(evt:MouseEvent):void{
			if (!_isMoving){
				evt.currentTarget.alpha = Cell.DEFAULT_ALPHA;
			}
		}
		
		private function startMove(evt:MouseEvent):void{
			
			//select the cell
            var cell:Cell = Cell(evt.currentTarget);
			BoardCell(cell.parent).select(cell.colorCode);
			
			//initialize drag source
            var ds:DragSource = new DragSource();
            ds.addData(cell, "cell");
            
            //get valid cells
            if (!_validMoves[cell.column + "#" + cell.row]){
            	//TODO: integrate with turns
            	_player.turn = true;
            	var call:Object = _gameService.getValidMoves(_player, cell);
            	call.operation = "getValidMoves";
            	call.cell = cell;
            }
            
            //create proxy image and start drag
            var dragImage:IFlexDisplayObject = cell.createDragImage();
            DragManager.doDrag(cell, ds, evt, dragImage);
            _isMoving = true;
		} 
		
		public function dragEnterCell(evt:DragEvent):void{
			if (evt.dragSource.hasFormat("cell")){
				var cell:Cell = Cell (evt.dragSource.dataForFormat("cell"));
				var boardCell:BoardCell = BoardCell(evt.currentTarget);
				boardCell.addEventListener(DragEvent.DRAG_EXIT, dragExitCell);
				if (validMove(cell, boardCell)){
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
				var destination:Checker = new Checker();
				destination.row = boardCell.row;
				destination.column = boardCell.column;
				destination.color = cell.color;
				destination.characteristic = null;
				
				//do move in backend
				var move:Move = new Move();
				move.player = _player;
				move.current = cell;
				move.destination = destination;
				move.status = Move.UNVERIFIED;
            	_gameService.doMove(move);
				
				//do move in interface
				BoardCell(cell.parent).reset();
				boardCell.reset();
				_board.moveCellToBoardCell(cell, boardCell);
			}
		}
		
		public function dragExitCell(evt:DragEvent):void{
			if (evt.dragSource.hasFormat("cell")){
				var cell:Cell = Cell (evt.dragSource.dataForFormat("cell"));
				var boardCell:BoardCell = BoardCell(evt.currentTarget);
				boardCell.reset();
			}
		} 
		
		private function endMove(evt:DragEvent):void{
			if (evt.dragSource.hasFormat("cell")){
				_isMoving = false;
				var cell:Cell = Cell(evt.currentTarget);
				cell.alpha = Cell.DEFAULT_ALPHA;
				BoardCell(cell.parent).reset();
			}
		}
		
		override protected function createChildren():void {
			
			//create board
			_board = new CheckersBoard(8, 8, dragEnterCell, dragDropCell, dragExitCell);
			addChild(_board);
		}
		
		override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void{
			
			//redefine cell size of board and position in center of game
			if (unscaledWidth / _board.nCols >= unscaledHeight / _board.nRows){
				
				//board limited by height, expand to height of game and center x
				_board.cellSize = unscaledHeight / _board.nRows;
				_board.x = (unscaledWidth - _board.cellSize * _board.nCols) / 2;
				_board.y = 0;
				
			}else{
				
				//board limited by width, expand to width of game and center y
				_board.cellSize = unscaledWidth / _board.nCols;
				_board.x = 0;
				_board.y = (unscaledHeight - _board.cellSize * _board.nRows) / 2;
			}
		}
		
		private function defineValidMoves(validMoves:Object, cell:Cell):void{
			_validMoves[cell.column + "#" + cell.row] = validMoves;
		}
		
		private function validMove(cell:Cell, boardCell:BoardCell):Boolean{
			var validCellMoves:Object = _validMoves[cell.column + "#" + cell.row];
			if (validCellMoves && validCellMoves[boardCell.column + "#" + boardCell.row]){
				return true
			}else{
				return false;
			}
		}
		

	}
}