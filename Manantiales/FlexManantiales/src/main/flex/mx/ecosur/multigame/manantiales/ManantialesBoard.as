    /*
    * Copyright (C) 2009 ECOSUR, Andrew Waterman
    *
    * Licensed under the Academic Free License v. 3.2.
    * http://www.opensource.org/licenses/afl-3.0.php
    */

    /**
    * @author awaterma@ecosur.mx
    */

    package mx.ecosur.multigame.manantiales
    {
    import flash.display.Shape;
    import flash.geom.Point;
    import flash.geom.Rectangle;
    
    import mx.collections.ArrayCollection;
    import mx.containers.Canvas;
    import mx.controls.Alert;
    import mx.ecosur.multigame.component.AbstractBoard;
    import mx.ecosur.multigame.component.BoardCell;
    import mx.ecosur.multigame.entity.GamePlayer;
    import mx.ecosur.multigame.enum.Color;
    import mx.ecosur.multigame.enum.manantiales.TokenType;
    import mx.ecosur.multigame.manantiales.token.ManantialesToken;

    /**
    * A ManantialesBoard is similar to the GenteBoard interfaces,
    * but cells are spaced in a much different manner, and not all
    * iumns and rows are filled in with actionable BoardCells.
    *
    * Generating a board like:
    *
    *
    *  M[B]  I[B]       M[B]   I[G]   M[G]
    *    M[B]  M[B]     I[B]  M[G]  M[G]
    *  I[B]  I[B]       F[G]   I[G]   I[G]
    *    F[B]  F[B]     F[G]  M[G]  F[G]
    *  F[B] F[B] M[R] M[R]  F[Y]  F[Y]  I[G]  F[G]
    *    F[R]  F[R]     F[R]  M[Y]  M[Y]
    *  I[R]  I[R]       M[R]   I[Y]   I[Y]
    *    I[R]  F[R]     I[Y]  F[Y]  M[Y]
    *   M[R]  I[R]      F[Y]   I[Y]   M[Y]
    *
    * This represents a 9x9 grid, but with spacing staggered
    * except for the center column and row.
    *
    * The center position of the board is always empty, acting
    * as the location of the Manantial or Spring for individuals
    * living in the Ejido.
    */
    public class ManantialesBoard extends AbstractBoard {

        protected var _bg:Shape, _hl:Shape, _vl:Shape,
          _manantial:Canvas, _spring:Shape;
        protected var _currentPlayer:GamePlayer;
        protected var _centerX:int, _centerY:int;
        protected var _graph:AdjGraph;

        public function ManantialesBoard () {
            super();
            this._nCols = 9;
            this._nRows = 9;
        }

        public function get boardCells():ArrayCollection {
            var ret:ArrayCollection = new ArrayCollection();

            for (var row:int = 0; row < _nRows; row++) {
                for (var col:int = 0; col < _nCols; col++) {
                    if (_boardCells [ col ][ row ] != null) {
                        var cell:RoundCell = RoundCell(_boardCells[col][row]);
                        var token:ManantialesToken = ManantialesToken(cell.token);
                        if (token && token.type != TokenType.UNDEVELOPED)
                            ret.addItem(token);
                    }
                }
            }

            return ret;
        }

        private function get center ():Point {
            return new Point (_centerX, _centerY);
        }
        
        function get currentPlayer ():GamePlayer {
            return _currentPlayer;
        }

        function set currentPlayer (currentPlayer:GamePlayer):void {
            this._currentPlayer = currentPlayer;
        }

        /* Component overrides */
        override protected function createChildren():void {
            _bg = new Shape();
            addChild(_bg);
			
            _manantial = new Canvas();
            _manantial.clipContent = false;
            _manantial.rotation = 45;
            _manantial.alpha = .55;
            addChild(_manantial);

            _spring = new Shape();
            addChild(_spring);

            _hl = new Shape();
            addChild(_hl);

            _vl = new Shape();
            addChild(_vl);

            createCells();

            _graph = createGraph();
            _cellsCreated = true;
        }

        protected function createCells():void {
            _boardCells = new Array();
            var counter:int = 0;
            var boardCell:RoundCell;
            for (var column:int = 0; column < 9; column++) {
                _boardCells[column] = new Array();
                for (var row:int = 0; row < 9; row++) {
                    boardCell = null;

                    if (row == 4) {
                        this.setStyle("cellBgColor", 0xA0A0A0);
                    } else if (column == 4) {
                        this.setStyle("cellBgColor", 0xA0A0A0);
                    } else if (column < 4 && row < 4) {
                        this.setStyle("cellBgColor", Color.getColorCode(Color.YELLOW));
                    } else if (column > 4 && row < 4) {
                        this.setStyle("cellBgColor", Color.getColorCode(Color.RED));
                    } else if (column < 4 && row > 4) {
                        this.setStyle("cellBgColor", Color.getColorCode(Color.PURPLE));
                    } else if (column > 4 && row > 4 && column > 4) {
                        this.setStyle("cellBgColor", Color.getColorCode(Color.BLACK));
                    }

                    /* Center Manantial */
                    if (column == 4 && row == 4) {
                        continue;
                    }
                    else if (row % 2 == 0 && column % 2 == 0) {
                        boardCell = new RoundCell(column, row, cellBgColor,
                                cellBorderColor, cellBorderThickness);
                    } else if (row % 2 != 0 && column % 2 != 0) {
                        boardCell = new RoundCell(column, row, cellBgColor,
                                cellBorderColor, cellBorderThickness);
                    } else if (row == 4 || column == 4) {
                        boardCell = new RoundCell(column, row, cellBgColor,
                                cellBorderColor, cellBorderThickness);
                    }

                    if (boardCell != null) {
                        addChild(boardCell);
                        boardCell.setStyle("padding", cellPadding);
                        _boardCells[column][row] = boardCell;
                        counter++;
                    }
                }
            }
        }

        override protected function updateDisplayList(unscaledWidth:Number,
            unscaledHeight:Number):void
       {
            // Check that _boardCells have been created
            if (!_cellsCreated){
                return;
            }

            var boardCell:RoundCell;
            var cellSize:Number;
            var factor:int, positioningFactor:int;

            var xPos:Number, yPos:Number, centerY:Number, centerX:Number;
            var endY:Number, endX:Number;

            factor = 3;
            positioningFactor = 10;

            this.measure();
            cellSize =  (this.measuredWidth/_nCols)/factor;
            tokenSize = (cellSize * 1.5) + (2 * cellPadding);

            for (var col:Number = 0; col < _nCols; col++){
                for (var row:Number = 0; row < _nRows; row++){
                       /* Pull cell sprite, and size based on current conditions */
                    boardCell = RoundCell(getBoardCell(col, row));
                    if (boardCell != null) {
                        boardCell.width = cellSize * 1.5;
                        boardCell.height = cellSize * 1.5;
                        xPos = cellSize * factor;
                        yPos = cellSize * factor;
                        boardCell.x = xPos * col;
                        boardCell.y = yPos * row;

                        /* Find corners and center */
                        if (col == 8) {
                            xPos = xPos + (cellSize / positioningFactor);
                            endX = xPos * col + (cellSize + cellSize / 2);
                        }

                        if (row == 8) {
                            yPos = yPos + (cellSize / positioningFactor);
                            endY = yPos * row + (cellSize + cellSize / 2);
                        }

                        if (row == 4) {
                            centerY = (yPos * row) + boardCell.height / 3;
                        } else if (col == 4) {
                            centerX = (xPos * col) + boardCell.width / 3;
                        }
                    }
                }
            }

            /* Board background */
            _bg.graphics.clear();
            _bg.graphics.beginFill(0xFFFFFF);
            _bg.graphics.lineStyle(1, 0xffffff, 3);
            _bg.graphics.drawRoundRect(0, 0, endX, endY, 15, 15);
            _bg.graphics.endFill();

            var size:Number = cellSize * 4;
            var linecompensation:Number = boardCell.height/ 3;

				/* Inlay the Manantial */
            _manantial.x = centerX + linecompensation/2;
            _manantial.y = centerY + linecompensation/2;
            _manantial.graphics.clear();
            _manantial.graphics.beginFill(0x6CA5E5);
            _manantial.graphics.lineStyle(1, 0xE4961A, 0.65);
            _manantial.graphics.drawRect(-size/2, -size/2, size, size);
            _manantial.graphics.endFill();

            _spring.graphics.clear();
            _spring.graphics.beginFill(0x2e83bc);
            _spring.graphics.lineStyle(1, 0x007CD3);
            _spring.graphics.drawCircle(centerX + linecompensation/2,
                centerY + linecompensation/2, size/4);
            _spring.graphics.endFill();


                /* Inlay the water feature */
            _hl.graphics.clear();
            _hl.graphics.beginFill(0x2E83BC);
            _hl.graphics.lineStyle(2, 0x2E83BC, 0.65);
            _hl.graphics.drawRect(0, centerY, endX, boardCell.height/3);
            _hl.graphics.endFill();

            _vl.graphics.clear();
            _vl.graphics.beginFill(0x2E83BC);
            _vl.graphics.lineStyle(2, 0x2E83BC, 0.65);
            _vl.graphics.drawRect(centerX, 0, boardCell.width/3, endY);
            _vl.graphics.endFill();

           drawGraph();
            _centerX = centerX;
            _centerY = centerY;
        }

        /* Draws the edges of the game graph */
        private function drawGraph():void {
            if (_graph == null)
                return;
            var nodes:Array = _graph.getNodes();
            var N:int = nodes.length;
            for (var i:int = 0; i < N; i++) {
                var a:Point, origin:RoundCell;
                a = nodes [ i ];
                origin = _boardCells [a.y] [a.x];
                for (var j:int = N; j > i; j--) {
                    if ( _graph.containsEdge(i, j)) {
                        var b:Point, dest:RoundCell;
                        b = nodes [ j ];
                        dest = _boardCells [ b.y ] [ b. x ];
                        _bg.graphics.beginFill(Color.getColorCode(Color.BLUE));
                        _bg.graphics.lineStyle(3, Color.getColorCode(Color.BLUE), 0.70);
                        _bg.graphics.moveTo(origin.y + (origin.height / 2), origin.x + (origin.width / 2));
                        _bg.graphics.lineTo(dest.y + (dest.height /2 ), dest.x + (dest.width / 2));
                        _bg.graphics.endFill();
                    }
                }
            }
        }

        /* Nodes are numbered from left to right, then down, and repeat. E.G. (0,0) = 0th node, (0,2) == 5th node */
        public function createGraph():AdjGraph {
            var ret:AdjGraph = new AdjGraph(48);
            var counter:int = 0;
            for (var row:int = 0; row < _nRows; row++) {
                for (var col:int = 0; col < _nCols; col++) {
                    var cell:BoardCell = _boardCells [col][row];
                    if (cell != null) {
                        var pt:Point = new Point();
                        pt.x = cell.row;
                        pt.y = cell.column;
                        cell.toolTip = counter + "[" + pt.y + "," + pt.x + "] @ "
                                + "(" + cell.y + "," + cell.x + ")";
                        ret.setPoint(counter,  pt);
                        counter = counter + 1;
                    }
                }
           }

            /* Edges only valid if _nRows = 9 and _nCols = 9, e.g. 8x8 grid ; */

            ret.addEdge(0, 1);
            ret.addEdge(0, 5);
            ret.addEdge(0, 10);
            ret.addEdge(1, 5);
            ret.addEdge(1, 6);
            ret.addEdge(1, 2);
            ret.addEdge(2, 6);
            ret.addEdge(2, 7);
            ret.addEdge(2, 8);
            ret.addEdge(2, 3);
            ret.addEdge(3, 8);
            ret.addEdge(3, 9);
            ret.addEdge(3, 4);
            ret.addEdge(4, 9);
            ret.addEdge(4, 14);
            ret.addEdge(5, 10);
            ret.addEdge(5, 11);
            ret.addEdge(5, 6);
            ret.addEdge(5, 15);
            ret.addEdge(6, 11);
            ret.addEdge(6, 16);
            ret.addEdge(6, 12);
            ret.addEdge(6, 7);
            ret.addEdge(7, 12);
            ret.addEdge(7, 8);
            ret.addEdge(8, 12);
            ret.addEdge(8, 18);
            ret.addEdge(8, 13);
            ret.addEdge(8, 9);
            ret.addEdge(9, 13);
            ret.addEdge(9, 19);
            ret.addEdge(9, 14);
            ret.addEdge(10, 20);
            ret.addEdge(10, 15);
            ret.addEdge(11, 15);
            ret.addEdge(11, 16);
            ret.addEdge(12, 16);
            ret.addEdge(12, 17);
            ret.addEdge(12, 18);
            ret.addEdge(13, 18);
            ret.addEdge(13, 19);
            ret.addEdge(14, 19);
            ret.addEdge(14, 27);
            ret.addEdge(15, 16);
            ret.addEdge(15, 20);
            ret.addEdge(15, 21);
            ret.addEdge(15, 22);
            ret.addEdge(16, 22);
            ret.addEdge(16, 23);
            ret.addEdge(16, 17);
            ret.addEdge(17, 23);
            ret.addEdge(17, 24);
            ret.addEdge(17, 18);
            ret.addEdge(18, 24);
            ret.addEdge(18, 25);
            ret.addEdge(18, 19);
            ret.addEdge(19, 25);
            ret.addEdge(19, 26);
            ret.addEdge(19, 27);
            ret.addEdge(20, 33);
            ret.addEdge(20, 28);
            ret.addEdge(20, 21);
            ret.addEdge(21, 22);
            ret.addEdge(21, 28);
            ret.addEdge(22, 28);
            ret.addEdge(22, 29);
            ret.addEdge(22, 23);
            ret.addEdge(23, 29);
            ret.addEdge(23, 30);
            ret.addEdge(24, 30);
            ret.addEdge(24, 31);
            ret.addEdge(24, 25);
            ret.addEdge(25, 31);
            ret.addEdge(25, 32);
            ret.addEdge(25, 26);
            ret.addEdge(26, 32);
            ret.addEdge(26, 27);
            ret.addEdge(27, 32);
            ret.addEdge(27, 37);
            ret.addEdge(28, 33);
            ret.addEdge(28, 34);
            ret.addEdge(28, 38);
            ret.addEdge(28, 29);
            ret.addEdge(29, 34);
            ret.addEdge(29, 39);
            ret.addEdge(29, 35);
            ret.addEdge(29, 30);
            ret.addEdge(30, 35);
            ret.addEdge(30, 35);
            ret.addEdge(30, 31);
            ret.addEdge(31, 35);
            ret.addEdge(31, 41);
            ret.addEdge(31, 36);
            ret.addEdge(31, 32);
            ret.addEdge(32, 36);
            ret.addEdge(32, 42);
            ret.addEdge(32, 37);
            ret.addEdge(33, 43);
            ret.addEdge(33, 38);
            ret.addEdge(34, 38);
            ret.addEdge(34, 39);
            ret.addEdge(35, 39);
            ret.addEdge(35, 40);
            ret.addEdge(35, 31);
            ret.addEdge(35, 41);
            ret.addEdge(36, 41);
            ret.addEdge(36, 42);
            ret.addEdge(37, 42);
            ret.addEdge(37, 47);
            ret.addEdge(38, 43);
            ret.addEdge(38, 44);
            ret.addEdge(38, 39);
            ret.addEdge(39, 44);
            ret.addEdge(39, 45);
            ret.addEdge(39, 40);
            ret.addEdge(40, 41);
            ret.addEdge(40, 45);
            ret.addEdge(41, 45);
            ret.addEdge(41, 46);
            ret.addEdge(41, 42);
            ret.addEdge(42, 46);
            ret.addEdge(42, 47);
            ret.addEdge(43, 44);
            ret.addEdge(44, 45);
            ret.addEdge(45, 46);
            ret.addEdge(46, 47);
            return ret;
        }
    }
}
