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

    import mx.collections.ArrayCollection;
    import mx.ecosur.multigame.component.AbstractBoard;
    import mx.ecosur.multigame.entity.GamePlayer;
    import mx.ecosur.multigame.enum.Color;
    import mx.ecosur.multigame.entity.manantiales.Ficha;
    import mx.ecosur.multigame.entity.manantiales.ManantialesGame;
    import mx.ecosur.multigame.enum.manantiales.TokenType;
    import mx.ecosur.multigame.manantiales.token.ManantialesToken;
    import mx.states.State;

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
          _manantial:Shape, _spring:Shape;
        protected var _currentPlayer:GamePlayer;
        protected var _centerX:int, _centerY:int;
        protected var classicMode:State, silvoMode:State;

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
                        var cell:RoundCell = RoundCell (_boardCells[col][row]);
                        var token:ManantialesToken = ManantialesToken (cell.token);
                        if (token && token.type != TokenType.UNDEVELOPED)
                            ret.addItem(token);
                    }
                }
            }

            return ret;
        }

        public function get center ():Point {
            return new Point (_centerX, _centerY);
        }
        
        public function get currentPlayer ():GamePlayer {
            return _currentPlayer;
        }

        public function set currentPlayer (currentPlayer:GamePlayer):void {
            this._currentPlayer = currentPlayer;
        }

        /* Component overrides */
        override protected function createChildren():void {
            _bg = new Shape();
            addChild(_bg);

            _manantial = new Shape();
            addChild(_manantial);

            _spring = new Shape();
            addChild(_spring);

            _hl = new Shape();
            addChild(_hl);

            _vl = new Shape();
            addChild(_vl);

            _boardCells = new Array();
            var counter:int = 0;
            var boardCell:RoundCell;

            for (var row:int = 0; row < 9; row++) {
                _boardCells[row] = new Array();
                for (var column:int = 0; column < 9; column++) {
                    boardCell = null;

                    if (row == 4) {
                        this.setStyle("cellBgColor", 0xA0A0A0);
                    } else if (column == 4) {
                        this.setStyle("cellBgColor", 0xA0A0A0);
                    } else if (column < 4 && row < 4) {
                        this.setStyle("cellBgColor", Color.getColorCode(Color.YELLOW));
                    } else if (column > 4 && row < 4) {
                        this.setStyle("cellBgColor", Color.getColorCode(Color.PURPLE));
                    } else if (column < 4 && row > 4) {
                        this.setStyle("cellBgColor", Color.getColorCode(Color.RED));
                    } else if (column > 4 && row > 4 && column > 4) {
                        this.setStyle("cellBgColor", Color.getColorCode(Color.BLACK));
                    }

                    /* Center Manantial */
                    if (column == 4 && row == 4) {
                        continue;
                    }
                    else if ( row % 2 == 0 && column % 2 == 0) {
                        boardCell = new RoundCell(column,row, cellBgColor,
                            cellBorderColor, cellBorderThickness);
                    } else if ( row % 2 != 0 && column % 2 != 0 ) {
                        boardCell = new RoundCell(column, row, cellBgColor,
                            cellBorderColor, cellBorderThickness);
                    } else if (row == 4 || column == 4) {
                        boardCell = new RoundCell(column,row, cellBgColor,
                            cellBorderColor, cellBorderThickness);
                    }

                    if (boardCell != null) {
                        boardCell.toolTip = column + "," + row;
                        addChild(boardCell);
                        _boardCells[row][column] = boardCell;
                        boardCell.setStyle("padding", cellPadding);
                        boardCell.alpha = 0.70;
                    }

                    counter = counter + 1;

                }
            }

            _cellsCreated = true;
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

            factor = 2;
            positioningFactor = 14.5;

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

                        /* starting value for displacement */
                        xPos = cellSize * factor;
                        yPos = cellSize * factor;

                        /* Hand adjustments for the staggered cells */
                        if (col == 1 || col == 3) {
                            if( row == 1 || row == 3) {
                                xPos = (cellSize - cellSize/positioningFactor)
                                    * factor;
                                yPos  = (cellSize - cellSize/positioningFactor)
                                    * factor;

                            } else if (row == 5 || row == 7) {
                                xPos = (cellSize - cellSize/positioningFactor)
                                    * factor;
                                yPos = (cellSize + cellSize/positioningFactor)
                                    * factor;
                            }
                        } else if (col == 2) {
                            if (row == 2) {
                                xPos = (cellSize - cellSize/positioningFactor)
                                    * factor;
                                yPos = (cellSize - cellSize/positioningFactor)
                                    * factor;
                            } else if (row == 6) {
                                xPos = (cellSize - cellSize/positioningFactor)
                                    * factor;
                                yPos = (cellSize + cellSize/positioningFactor)
                                    * factor;
                            }
                        } else if (col == 5 || col == 7) {
                            if (row == 1 || row == 3) {
                                xPos = (cellSize + cellSize/positioningFactor)
                                    * factor;
                                yPos = (cellSize - cellSize/positioningFactor)
                                    * factor;
                            } if (row == 5 || row == 7) {
                                xPos = (cellSize + cellSize/positioningFactor)
                                    * factor;
                                yPos = (cellSize + cellSize/positioningFactor)
                                    * factor;
                            }

                        } else if (col == 6) {
                            if (row == 2) {
                                xPos = (cellSize + cellSize/positioningFactor)
                                    * factor;
                                yPos = (cellSize - cellSize/positioningFactor)
                                    * factor;
                            } else if (row == 6) {
                                xPos = (cellSize + cellSize/positioningFactor)
                                    * factor;
                                yPos = (cellSize + cellSize/positioningFactor)
                                    * factor;
                            }
                        } else if (col == 8) {
                            xPos = xPos + (cellSize/positioningFactor);
                            endX = xPos * col + (cellSize + cellSize/2);
                        }

                        if (row == 8) {
                            yPos = yPos + (cellSize/positioningFactor);
                            endY = yPos * row + (cellSize + cellSize/2);
                        }

                        if (row == 4) {
                            centerY = (yPos * row) + boardCell.height/3;
                        } else if (col == 4) {
                            centerX = (xPos * col) + boardCell.width/3;
                        }

                        boardCell.x = xPos * col;
                        boardCell.y = yPos * row;

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
            _manantial.graphics.clear();
            _manantial.graphics.beginFill(0x6CA5E5);
            _manantial.graphics.lineStyle(1, 0xE4961A, 0.65);
            _manantial.graphics.drawRect(centerX - size/2 + linecompensation/2,
                centerY - size/2 + linecompensation/2, size, size);
            _manantial.graphics.endFill();
            _manantial.alpha = .55;

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

            /* Flip the board to the player's perspective */
           /*
            this.rotation = boardRotation;
            var destPos:Point = findDestination();
            this.move(destPos.x, destPos.y);
            _centerX = centerX;
            _centerY = centerY;*/
        }

        protected function get boardRotation():int {
            var ret:int = 0;

            if (_currentPlayer.color == Color.BLACK) {
                ret = 90 * 3;
            } else if (_currentPlayer.color == Color.YELLOW) {
                ret = 90 * 2;
            } else if (_currentPlayer.color == Color.RED) {
                ret = 90 * 1;
            }

            return ret;
        }

        protected function findDestination ():Point {
            var ret:Point = new Point();

            if (_currentPlayer.color == Color.BLACK) {
                ret.y = y + _bg.width;
            } else if (_currentPlayer.color == Color.YELLOW) {
                ret.x = x + _bg.height;
                ret.y = y + _bg.width;
            } else if (_currentPlayer.color == Color.RED) {
                ret.x = x + _bg.height;
            }

            return ret;
        }
    }
}
