/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.2. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 *  @author max@alwayssunny.com
 *  @author awaterma@ecosur.mx
*/

package mx.ecosur.multigame.component {
    
    import flash.events.Event;

    import flash.utils.getQualifiedClassName;
    
    import mx.core.UIComponent;
    import mx.events.DragEvent;
    
    [Style(name="cellBgColor", type="uint", format="Color")]
    [Style(name="cellBgAlpha", type="Number", format="Length")]
    [Style(name="cellBorderColor", type="uint", format="Color")]
    [Style(name="cellBorderThickness", type="int", format="Length")]
    [Style(name="cellPadding", type="int", format="Length")]
    [ResourceBundle("Commons")]
    public class AbstractBoard extends UIComponent {
        
        protected var _nCols:int; //number of cells on X axis
        protected var _nRows:int; //number of cells of Y axis
        protected var _boardCells:Array; //two dimensional array of BoardCell
        protected var _columnLabels:Array; //array of text field column labels
        
        [Bindable]
        public var tokenSize:Number; //size of tokens
        
        // Default cell style properties
        protected static const DEFAULT_CELL_BG_COLOR:uint = 0xffffff;
        protected static const DEFAULT_CELL_BG_ALPHA:Number = 1;
        protected static const DEFAULT_CELL_BORDER_COLOR:uint = 0x000000;
        protected static const DEFAULT_CELL_BORDER_THICKNESS:uint = 1;
        protected static const DEFAULT_CELL_PADDING:Number = 2.5;

        // Flags
        protected var _cellsCreated:Boolean;
        
        // Listeners
        protected var _dragEnterHandler:Function;
        protected var _dragExitHandler:Function;
        protected var _dragDropHandler:Function;
        
        public function AbstractBoard() {
            super();
            
            if(getQualifiedClassName(this) == "mx.ecosur.multigame.component::AbstractGameBoard") {
                throw new Error("Unable to instantiate an Abstract Class!");
            }

            _cellsCreated = false;
            addEventListener(Event.RESIZE, function():void{invalidateSize()});
        }
        
        /* Getters and setters */
        
        public function get nRows():int{
            return _nRows;
        }
        
        public function set nRows(nRows:int):void{
            if (_nRows != nRows){
                _nRows = nRows;
                invalidateDisplayList();
            }
        }
        
        public function get nCols():int{
            return _nCols;
        }
        
        public function set nCols(nCols:int):void{
            if (_nCols != nCols){
                _nCols = nCols;
                invalidateDisplayList();
            }
        }
        
        public function set dragEnterHandler(dragEnterHandler:Function):void{
            _dragEnterHandler = dragEnterHandler;
            invalidateProperties();
        }
        
        public function set dragDropHandler(dragDropHandler:Function):void{
            _dragDropHandler = dragDropHandler;
            invalidateProperties();
        }
        
        public function set dragExitHandler(dragExitHandler:Function):void{
            _dragExitHandler = dragExitHandler;
            invalidateProperties();
        }
        
        public function getBoardCell(colNum:int, rowNum:int):BoardCell {
            if (colNum < _boardCells.length && rowNum < _boardCells[colNum].length){
                return BoardCell (_boardCells[colNum][rowNum]);
            }else{
                return null;
            }
        }
        
        /* Public functions */
        
        /**
         * Adds a Token to the board. The token should contain a Cell
         * with defined row and column values.
         *  
         * @param token the token to add to the board.
         */
        public function addToken(token:Token):void{
            var boardCell:BoardCell = getBoardCell(token.cell.column, token.cell.row); 
            boardCell.token = token;
        }
                
        /**
         * Removes all tokens from all board cells. This has the
         * effect of cleaning the board. 
         */
        public function clearTokens():void{
            if (_boardCells == null || _boardCells.length == 0){
                return;
            }
            var boardCell:BoardCell;
            for (var i:Number = 0; i < _nCols; i++){
                for (var j:Number = 0; j < _nRows; j++){
                    boardCell = BoardCell(_boardCells[i][j]);
                    if (boardCell != null)
                        boardCell.token = null;
                }
            }
        }
        
        /**
         * Returns a string representation of a location on the board
         *  
         * @param column the column number (first column is 0)
         * @param row the row numer (first row is 0)
         * @return a description of the location on the board
         * 
         */
        public function getCellDescription(column:Number, row:Number):String{
            return resourceManager.getString("Commons", "move.history.row") + " " + (nRows - row) +
                    ", " + resourceManager.getString("Commons", "move.history.column") + " " + (column + 1);
        }
        
        override protected function measure():void{
            
            // Define minimum size
            measuredMinWidth = 300;
            measuredMinHeight = 300;
            
            // Define preferred size
            if (unscaledWidth / _nCols >= unscaledHeight / _nRows){
                
                // Board limited by height
                measuredWidth = _nCols * unscaledHeight / _nRows;
                measuredHeight = unscaledHeight;
                  
            } else { 
                
                // Board limited by width
                measuredHeight = _nRows * unscaledWidth / _nCols;
                measuredWidth = unscaledWidth;
            }
        }
        
        override protected function commitProperties():void{
            
            // Check that boardCells have been created
            if (!_cellsCreated){
                return;
            }
            
            //reset handlers
            for (var i:Number = 0; i < _nCols; i++){
                for (var j:Number = 0; j < _nRows; j++){
                	var boardCell:BoardCell = getBoardCell(i,j);
                    if (_dragExitHandler != null && boardCell != null){
                        boardCell.addEventListener(DragEvent.DRAG_EXIT, _dragExitHandler);
                    }
                    if (_dragEnterHandler != null && boardCell != null){
                        boardCell.addEventListener(DragEvent.DRAG_ENTER, _dragEnterHandler);
                    }
                    if(_dragDropHandler != null && boardCell != null){
                        boardCell.addEventListener(DragEvent.DRAG_DROP, _dragDropHandler);
                    }
                }
            }
        }
        
        /* Style management */  

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
