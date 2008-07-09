package mx.ecosur.multigame.pente {
	import mx.core.UIComponent;
	import mx.ecosur.multigame.entity.BoardCell;
	import mx.ecosur.multigame.entity.Cell;
	import mx.events.DragEvent;
	import mx.events.FlexEvent;
	
	[Style(name="cellBgColor", type="uint", format="Color")]
	[Style(name="cellBgAlpha", type="Number", format="Length")]
	[Style(name="cellBorderColor", type="uint", format="Color")]
	[Style(name="cellBorderThickness", type="int", format="Length")]
	[Style(name="cellPadding", type="int", format="Length")]
	
	public class PenteBoard extends UIComponent {
		
		private var _nCols:int; //number of cells on X axis
		private var _nRows:int; //number of cells of Y axis
		private var _boardCellSize:Number; //size of cell containers. BoardCell are presumed to be square.
		private var boardCells:Array; //two dimensional array of BoardCell
		
		//default cell style properties
		private static const DEFAULT_CELL_BG_COLOR:uint = 0xffffff;
		private static const DEFAULT_CELL_BG_ALPHA:Number = 1;
		private static const DEFAULT_CELL_BORDER_COLOR:uint = 0x000000;
		private static const DEFAULT_CELL_BORDER_THICKNESS:uint = 1;
		private static const DEFAULT_CELL_PADDING:Number = 5;
		
		//dirty flags
		private var _cellsDirty:Boolean;
		private var _doResize:Boolean;
		private var _dragEnterHandler:Function;
		private var _dragExitHandler:Function;
		private var _dragDropHandler:Function;
		
		
		/**
		 * Contructor
		 *  
		 * @param nCols the number of columns
		 * @param nRows the number of rows
		 * @param dragEnterHandler
		 * @param dragDropHandler
		 * @param dragExitHandler
		 * 
		 */
		public function PenteBoard(nCols:int, nRows:int, dragEnterHandler:Function, dragDropHandler:Function, dragExitHandler:Function){
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
		
		public function get boardCellSize():Number{
			return _boardCellSize;
		}
		
		public function set boardCellSize(boardCellSize:Number):void{
			if (_boardCellSize != boardCellSize){
				_boardCellSize = boardCellSize;
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
		
		public function clearCells():void{
			var boardCell:BoardCell;
			for (var i:Number = 0; i < _nCols; i++){
				for (var j:Number = 0; j < _nRows; j++){
					boardCell = BoardCell(boardCells[i][j]);
					boardCell.cell = null;
				}
			}
		}
		
		override protected function createChildren():void {
			
			//create all cells
			var boardCell:BoardCell;
			boardCells = new Array();
			for (var i:Number = 0; i < _nCols; i++){
				boardCells[i] = new Array();
				for (var j:Number = 0; j < _nRows; j++){
					boardCell = new BoardCell(j, i, cellBgColor, cellBorderColor, cellBorderThickness);
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
		
//-------------------------------------------------------------------------------------------------------
// style management
//-------------------------------------------------------------------------------------------------------		
		

		public function get cellBgColor():uint {
			var prop:uint = getStyle("cellBgColor");
			return (prop != 0) ? prop : DEFAULT_CELL_BG_COLOR;
		}
		
		public function get cellBgAlpha():Number {
			var prop:uint = getStyle("cellBgAlpha");
			return (prop != 0) ? prop : DEFAULT_CELL_BG_ALPHA;
		}
		
		public function get cellBorderColor():uint {
			var prop:uint = getStyle("cellBorderColor");
			return (prop != 0) ? prop : DEFAULT_CELL_BORDER_COLOR;
		}
		
		public function get cellBorderThickness():Number {
			var prop:uint = getStyle("cellBorderThickness");
			return (prop != 0) ? prop : DEFAULT_CELL_BORDER_THICKNESS;
		}
		
		public function get cellPadding():Number {
			var prop:uint = getStyle("cellPadding");
			return (prop != 0) ? prop : DEFAULT_CELL_PADDING;
		}
	}
}