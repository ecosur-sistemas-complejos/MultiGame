package mx.ecosur.multigame.checkers {
	import mx.core.UIComponent;
	import mx.ecosur.multigame.entity.BoardCell;
	import mx.ecosur.multigame.entity.Cell;
	import mx.events.DragEvent;
	import mx.controls.Alert;
	
	[Style(name="cellBgColor1", type="uint", format="Color")]
	[Style(name="cellBgColor2", type="uint", format="Color")]
	[Style(name="cellBgAlpha", type="Number", format="Length")]
	[Style(name="cellBorderColor", type="uint", format="Color")]
	[Style(name="cellBorderThickness", type="int", format="Length")]
	[Style(name="cellPadding", type="int", format="Length")]
	
	public class CheckersBoard extends UIComponent {
		
		private var _nCols:int; //number of cells on X axis
		private var _nRows:int; //number of cells of Y axis
		private var _boardCellSize:Number; //size of cell containers. BoardCell are presumed to be square.
		private var boardCells:Array; //two dimensional array of BoardCell
		
		//default cell style properties
		private static const DEFAULT_CELL_BG_COLOR_1:uint = 0x000000;
		private static const DEFAULT_CELL_BG_COLOR_2:uint = 0xffffff;
		private static const DEFAULT_CELL_BG_ALPHA:Number = 1;
		private static const DEFAULT_CELL_BORDER_COLOR:uint = 0x000000;
		private static const DEFAULT_CELL_BORDER_THICKNESS:uint = 0;
		private static const DEFAULT_CELL_PADDING:Number = 5;
		
		//dirty flags
		private var _cellsDirty:Boolean;
		private var _doResize:Boolean;
		private var _dragEnterHandler:Function;
		private var _dragExitHandler:Function;
		private var _dragDropHandler:Function;
		
		public function CheckersBoard(nCols:int, nRows:int, dragEnterHandler:Function, dragDropHandler:Function, dragExitHandler:Function){
			super();
			_dragDropHandler = dragDropHandler;
			_dragEnterHandler = dragEnterHandler;
			_dragExitHandler = dragExitHandler;
			_nRows = nRows;
			_nCols = nCols;
		}
		
		public function get nRows():int{
			return _nRows;
		}
		
		public function get nCols():int{
			return _nCols;
		}
		
		public function get cellSize():Number{
			return _boardCellSize;
		}
		
		public function set cellSize(cellSize:Number):void{
			if (_boardCellSize != cellSize){
				_boardCellSize = cellSize;
				_doResize = true;
				invalidateDisplayList();
			}
		}
		
		public function getBoardCell(colNum:int, rowNum:int):BoardCell {
			if (colNum < boardCells.length && rowNum < boardCells[colNum].length){
				return BoardCell (boardCells[colNum][rowNum]);
			}else{
				return null;
			}
		}
		
		public function addCell(cell:Cell):void{
			getBoardCell(cell.column, cell.row).cell = cell;
		}
		
		public function moveCellToBoardCell(cell:Cell, boardCell:BoardCell):void{
			var oldBoardCell:BoardCell = BoardCell(cell.parent);
			oldBoardCell.cell = null;
			boardCell.cell = cell;
			cell.row = boardCell.row;
			cell.column = boardCell.column;
		}
		
		override protected function createChildren():void {
			
			//create all cells
			var boardCell:BoardCell;
			boardCells = new Array();
			for (var i:Number = 0; i < _nCols; i++){
				boardCells[i] = new Array();
				for (var j:Number = 0; j < _nRows; j++){
					if ((i + j) % 2 == 0){
						boardCell = new BoardCell(j, i, cellBgColor1, cellBorderColor, cellBorderThickness);
					}else{
						boardCell = new BoardCell(j, i, cellBgColor2, cellBorderColor, cellBorderThickness);
					}
					boardCells[i][j] = boardCell;
					addChild(boardCell);
					boardCell.setStyle("padding", cellPadding);
					boardCell.addEventListener(DragEvent.DRAG_ENTER, _dragEnterHandler);
					boardCell.addEventListener(DragEvent.DRAG_DROP, _dragDropHandler);
					boardCell.addEventListener(DragEvent.DRAG_EXIT, _dragExitHandler);
				}
			}
			_cellsDirty = true;
		}
		
		override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void{
			
			//if size of component has changed then scale and reposition cells 
			if (_doResize){
				_doResize = false;
				
				//loop through cells positioning and scaling them
				var boardCell:BoardCell;
				for (var i:Number = 0; i < _nCols; i++){
					for (var j:Number = 0; j < _nRows; j++){
						boardCell = getBoardCell(i, j);
						boardCell.width = _boardCellSize;
						boardCell.height = _boardCellSize;
						boardCell.x = _boardCellSize * i;
						boardCell.y = _boardCellSize * j;
					}
				}
			}
		}
		
//--------------------------------------------------------------------------------------------------------
// style management
//-------------------------------------------------------------------------------------------------------		
		

		private function get cellBgColor1():uint
		{
			var prop:uint = getStyle("cellBgColor1");
			return (prop != 0) ? prop : DEFAULT_CELL_BG_COLOR_1;
		}
		private function get cellBgColor2():uint
		{
			var prop:uint = getStyle("cellBgColor2");
			return (prop != 0) ? prop : DEFAULT_CELL_BG_COLOR_2;
		}
		private function get cellBgAlpha():Number
		{
			var prop:uint = getStyle("cellBgAlpha");
			return (prop != 0) ? prop : DEFAULT_CELL_BG_ALPHA;
		}
		private function get cellBorderColor():uint
		{
			var prop:uint = getStyle("cellBorderColor");
			return (prop != 0) ? prop : DEFAULT_CELL_BORDER_COLOR;
		}
		private function get cellBorderThickness():Number
		{
			var prop:uint = getStyle("cellBorderThickness");
			return (prop != 0) ? prop : DEFAULT_CELL_BORDER_THICKNESS;
		}
		private function get cellPadding():Number
		{
			var prop:uint = getStyle("cellPadding");
			return (prop != 0) ? prop : DEFAULT_CELL_PADDING;
		}
	}
}