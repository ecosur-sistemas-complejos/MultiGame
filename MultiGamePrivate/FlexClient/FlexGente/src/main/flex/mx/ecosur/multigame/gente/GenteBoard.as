/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.2. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author max@alwayssunny.com
*/

package mx.ecosur.multigame.gente {
    
    import flash.events.Event;

import flash.text.TextField;

import flash.text.TextFormat;

import mx.controls.Image;
    import mx.core.UIComponent;
    import mx.ecosur.multigame.component.BoardCell;
    import mx.ecosur.multigame.component.Token;
    import mx.events.DragEvent;
    
    [Style(name="cellBgColor", type="uint", format="Color")]
    [Style(name="cellBgAlpha", type="Number", format="Length")]
    [Style(name="cellBorderColor", type="uint", format="Color")]
    [Style(name="cellBorderThickness", type="int", format="Length")]
    [Style(name="cellPadding", type="int", format="Length")]
    [ResourceBundle("Gente")]
    public class GenteBoard extends UIComponent {
        
        private var _nCols:int; //number of cells on X axis
        private var _nRows:int; //number of cells of Y axis
        private var _boardCells:Array; //two dimensional array of BoardCell
        private var _columnLabels:Array; //array of text field column labels
        
        [Bindable]
        public var tokenSize:Number; //size of tokens
        
        [Embed(source='/assets/icons.swf#centerSquare')]
        private static var centerBgSource:Class;
        
        // Default cell style properties
        private static const DEFAULT_CELL_BG_COLOR:uint = 0xffffff;
        private static const DEFAULT_CELL_BG_ALPHA:Number = 1;
        private static const DEFAULT_CELL_BORDER_COLOR:uint = 0x000000;
        private static const DEFAULT_CELL_BORDER_THICKNESS:uint = 1;
        private static const DEFAULT_CELL_PADDING:Number = 2.5;
        private static const LABEL_SIZE:Number = 0;
        
        // Flags
        private var _cellsCreated:Boolean;
        
        // Listeners
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
        public function GenteBoard(){
            super();
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
            return " " + resourceManager.getString("Commons", "move.history.row") + " " + (nRows - row) +
                    ", " + resourceManager.getString("Commons", "move.history.column") + " " + (column + 1);
        }
        
        
        /* Component overrides */
        
        override protected function createChildren():void {
            
            // Create all cells
            var boardCell:BoardCell;
            _boardCells = new Array();
            for (var col:Number = 0; col< _nCols; col++){
                _boardCells[col] = new Array();
                for (var row:Number = 0; row < _nRows; row++){
                    boardCell = new BoardCell(col,row, cellBgColor, cellBorderColor, cellBorderThickness);
                    _boardCells[col][row] = boardCell;
                    super.addChild(boardCell);
                    boardCell.setStyle("padding", cellPadding);
                }
            }
            
            //add center square to center cell
            var centerCell:BoardCell = _boardCells[Math.floor(_nCols / 2)][Math.floor(_nRows / 2)];
            var img:Image = new Image();
            img.source = centerBgSource;
            centerCell.bgImage = img;                                   
            _cellsCreated = true;
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
        
        override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void{
            
            // Check that _boardCells have been created
            if (!_cellsCreated){
                return;
            }
                
            // Loop through cells positioning and scaling them
            // This assumes that the cells are square
            var boardCell:BoardCell;
            var boardCellSize:Number = (measuredWidth - LABEL_SIZE) / _nCols;
            var baseX:Number = (unscaledWidth - measuredWidth + LABEL_SIZE) / 2;
            tokenSize = boardCellSize - 2 * cellPadding;
            for (var col:Number = 0; col < _nCols; col++){
                for (var row:Number = 0; row < _nRows; row++){
                    boardCell = getBoardCell(col, row);
                    boardCell.width = boardCellSize;
                    boardCell.height = boardCellSize;
                    boardCell.x = baseX + LABEL_SIZE + boardCellSize * col;
                    boardCell.y = boardCellSize * row;
                }
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
                    if (_dragExitHandler != null){
                        getBoardCell(i, j).addEventListener(DragEvent.DRAG_EXIT, _dragExitHandler);
                    }
                    if (_dragEnterHandler != null){
                        getBoardCell(i, j).addEventListener(DragEvent.DRAG_ENTER, _dragEnterHandler);
                    }
                    if(_dragDropHandler != null){
                        getBoardCell(i, j).addEventListener(DragEvent.DRAG_DROP, _dragDropHandler);
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