/*
* Copyright (C) 2009 ECOSUR, Andrew Waterman
* 
* Licensed under the Academic Free License v. 3.2. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
*/

package mx.ecosur.multigame.oculto
{	
	import flash.display.Shape;
	import flash.geom.Point;

    import mx.controls.Alert;
    import mx.ecosur.multigame.component.AbstractBoard;
	import mx.ecosur.multigame.entity.GamePlayer;
	import mx.ecosur.multigame.enum.Color;
	import mx.states.State;

    import mx.ecosur.multigame.component.*;
    import mx.ecosur.multigame.pasale.token.*;
    
    /**
     *
     * A TablonBoard is built based upon the TablonGrid implementation, in order
     * to allow a great deal of 
     *
    */ 
	public class PasaleBoard extends AbstractBoard {
		
		protected var _bg:Shape, _hl:Shape, _vl:Shape;
		protected var _currentPlayer:GamePlayer;
        protected var _grid:TablonGrid;
		protected var _centerX:int, _centerY:int;
		
		public function PasaleBoard (grid:TablonGrid) {
			super();
            _grid = grid;
		}
		
		public function get center ():Point {
			return new Point (_centerX, _centerY);
		}
		
		public function set currentPlayer (currentPlayer:GamePlayer):void {
			this._currentPlayer = currentPlayer;
		}

         /**
         * Adds a Token to the board. The token should contain a Cell
         * with defined row and column values.
         *
         * @param token the token to add to the board.
         */
        override public function addToken(token:Token):void{
            var boardCell:BoardCell = getBoardCell(token.cell.column, token.cell.row);
            var ocultoToken:PasaleToken = PasaleToken (token);
/*            if (_currentPlayer.color != ocultoToken.cell.color) {
                Alert (_currentPlayer.color + "[currPlayer.color], " + ocultoToken.cell.color + "[ocultoToken]");
                ocultoToken.txt = null;
                ocultoToken.validateNow();
            }*/
            boardCell.token = ocultoToken;
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
            
            for (var i:int = 0; i < 9; i++) {
            	_boardCells[i] = new Array();
	            for (var j:int = 0; j < 9; j++) {          		            	
    	            boardCell = null;
    	            
    	            if (i == 4) {
    	            	this.setStyle("cellBgColor", 0xA0A0A0); 
    	            } else if (j == 4) {
    	            	this.setStyle("cellBgColor", 0xA0A0A0);
    	            } else if (i < 4 && j < 4) {
                    	this.setStyle("cellBgColor", Color.getColorCode(Color.BLUE));
                    } else if (i < 4 && j > 4) {
                        this.setStyle("cellBgColor", Color.getColorCode(Color.RED));
                    } else if (i > 4 && j < 4) {
                        this.setStyle("cellBgColor", Color.getColorCode(Color.BLACK));
                    } else if (i > 4 && j > 4) {
                        this.setStyle("cellBgColor", Color.getColorCode(Color.YELLOW));
                    }
    	            
    	            if (i == 4 && j == 4) {
    	            	continue;
    	            }
    	            else if ( i % 2 == 0 && j % 2 == 0) {
	            		boardCell = new RoundCell(j,i, cellBgColor, 
                            cellBorderColor, cellBorderThickness);  
	            	} else if ( i % 2 != 0 && j % 2 != 0 ) {
	                	boardCell = new RoundCell(j, i, cellBgColor, 
                            cellBorderColor, cellBorderThickness);                         
	                } else if (i == 4 || j == 4) {
                        boardCell = new RoundCell(j,i, cellBgColor, 
                            cellBorderColor, cellBorderThickness);  		                	
	                }
	                
	              	if (boardCell != null) {
	              		addChild(boardCell);
                        _boardCells[i][j] = boardCell;
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
            _manantial.graphics.beginFill(0xE4961A);
            _manantial.graphics.lineStyle(1, 0xE4961A, 0.65);
            _manantial.graphics.drawRect(centerX - size/2 + linecompensation/2,
                centerY - size/2 + linecompensation/2, size, size);
            _manantial.graphics.endFill();    
            
            _spring.graphics.clear();
            _spring.graphics.beginFill(0x2e83bc);
            _spring.graphics.lineStyle(1, 0x2e83bc);
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
            this.rotation = boardRotation;            
            var destPos:Point = findDestination();
            this.move(destPos.x, destPos.y);
            _centerX = centerX;
            _centerY = centerY;
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