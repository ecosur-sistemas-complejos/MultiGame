/*
* Copyright (C) 2010 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
*/

package mx.ecosur.multigame.pasale {

    import as3isolib.display.primitive.IsoBox;
    import as3isolib.display.scene.IsoScene;
    
    import as3isolib.geom.IsoMath;
    import as3isolib.geom.Pt;

    import as3isolib.graphics.SolidColorFill;

    import eDpLib.events.ProxyEvent;

    import flash.events.Event;
    import flash.events.MouseEvent;

    import mx.ecosur.multigame.component.AbstractBoard;
    import mx.ecosur.multigame.entity.Cell;
    import mx.ecosur.multigame.entity.GameGrid;
    import mx.ecosur.multigame.entity.GamePlayer;
    import mx.ecosur.multigame.enum.Color;
    import mx.ecosur.multigame.enum.MoveStatus;
    import mx.ecosur.multigame.pasale.entity.PasaleFicha;
    import mx.ecosur.multigame.pasale.entity.PasaleMove;
    import mx.ecosur.multigame.pasale.entity.PasalePlayer;
    import mx.ecosur.multigame.pasale.enum.UseType;
    import mx.effects.Sequence;
    import mx.events.DynamicEvent;
    import mx.managers.PopUpManager;

    public class PasaleBoard extends AbstractBoard {

        private static var DIMENSION:int = 22;

        protected var _pasalePlayer:PasalePlayer;

        protected var _blinkAnim:Sequence;

        private var _alert:ColonizerAlert;
        private var _grid:GameGrid;
        private var _scene:IsoScene;
        private var _box:PasaleBox;
        
        private var _controller:PasaleGameController;

        /* Tile sizes for different conditions */
        private var river:int = 10;
        private var riparian:int = 20;
        private var mountain:int = 45;

        private var _size:int = 0;

        public function PasaleBoard() {
            super();
            addEventListener(Event.RESIZE, function():void{invalidateSize()});
        }

        public function get controller():PasaleGameController {
            return _controller;
        }

        public function set controller(value:PasaleGameController):void {
            _controller = value;
        }

        public function set currentPlayer (currentPlayer:GamePlayer):void {
            this._pasalePlayer = PasalePlayer (currentPlayer);
        }

        public function get currentPlayer():GamePlayer {
            return _pasalePlayer;
        }

        public function get grid():GameGrid {
            return _grid;
        }

        public function set grid(value:GameGrid):void {
            _grid = value;
            update();
        }

        public function remove(event:ProxyEvent):void {
            _scene.removeChild(PasaleBox(event.target));
        }

        public function choose(event:ProxyEvent):void {
            if (_alert != null) {
                return;
            }
            /* Highlight the square */
            _box = PasaleBox(event.target);
            _box.height = _box.height + mountain;
            _box.render(true);
            
            _alert = new ColonizerAlert();
            _alert.addEventListener("build", colonize);
            _alert.addEventListener("cancel", cancelAlert);
            PopUpManager.addPopUp(_alert, this, true);
            PopUpManager.centerPopUp(_alert);
        }

        private function cancelAlert(event:DynamicEvent):void {
            PopUpManager.removePopUp(_alert);            
            _box.height = _box.height - mountain;
            _box.render(true);
            _box = null;
            _alert = null;
        }

        public function colonize(event:DynamicEvent):void {
            var targetType:String = _alert.type.selectedItem.data;

            /* clean up */
            _box.height = _box.height - mountain;
            _box.render(true);
            PopUpManager.removePopUp(_alert);
            _alert = null;            

            /* Send move across the wire */
            var move:PasaleMove = new PasaleMove();
            move.status = String (MoveStatus.UNVERIFIED);
            move.player = currentPlayer;
            move.id = 0;

            for (var i:int = 0; i < grid.cells.length; i++) {
                var cell:PasaleFicha = PasaleFicha(grid.cells.getItemAt(i));
                if (cell.column == _box.column && cell.row == _box.row) {
                    var destination:PasaleFicha = new PasaleFicha();
                    destination.column = cell.column;
                    destination.row = cell.row;
                    destination.type = targetType;
                    move.currentCell = cell;
                    move.destinationCell = destination;
                    break;
                }
            }

            if (move.destinationCell != null) {
                move.player = _pasalePlayer;
                _controller.sendMove(move);
            }

            _box = null;

            /* render the move */
            doMove(move);                        
        }

        public function doMove(move:PasaleMove):void {
            var destination:PasaleFicha = PasaleFicha (move.destinationCell);
            var idx:int = -1;

            for (var i:int = 0; i < grid.cells.length; i++) {
                var cell:PasaleFicha = PasaleFicha (grid.cells.getItemAt(i));
                if (cell.column == destination.column && cell.row == destination.row) {
                    idx = i;
                    break;
                }
            }

            grid.cells.removeItemAt(idx);
            grid.cells.addItemAt(destination, idx);
            update();
            
        }

        override protected function measure():void{
            measuredMinWidth = 500;
            measuredMinHeight = 500;

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

            findSize(measuredWidth, measuredHeight);
        }

        override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void {
            findSize (unscaledWidth, unscaledHeight);
            update();
        }

        public function get size():int {
            return _size;
        }

        public function set size (sze:int):void {
            _size = sze;
        }

        public function findSize(w:int, h:int):int {
            size = (w /DIMENSION)/2;
            return _size;
        }

        public function update ():void
        {
            if (grid == null)
                return;

            if (_scene == null) {
                _scene = new IsoScene();
                _scene.hostContainer = this;
            } else
                _scene.removeAllChildren();

            var pt:Pt = new Pt(width /2, height / 2);
            IsoMath.screenToIso(pt);

            var particle:int = size / 2;
            var events:Boolean;

            _scene.removeAllChildren();

            for (var i:int=0; i < _grid.cells.length; i++) {
                var cell:PasaleFicha = PasaleFicha (_grid.cells.getItemAt(i));
                var box:PasaleBox = drawCell (cell.column, cell.row, cell.type);
                box.moveTo(box.column * size + pt.x, box.row * size + pt.y, (height / 3));
                _scene.addChild(box);
            }
            _scene.render();
        }
        
        private function drawCell (column:int, row:int, type:String):PasaleBox {
            var events:Boolean = true;
            var box:PasaleBox = new PasaleBox();
            box.column = column;
            box.row = row;
            box.type = type;
            box.setSize(size,size,riparian);

            switch (box.type) {
                case UseType.WATER_PARTICLE:
                    box.fill = new SolidColorFill(Color.getColorCode(Color.BLUE), 0.48);
                    box.setSize(size,size, river);
                    break;
                case UseType.SOIL_PARTICLE:
                    box.fill = new SolidColorFill(0xAA9C82, 0.78);
                    box.setSize(size,size, river);
                    break;
                case UseType.FOREST:
                    box.fill = new SolidColorFill(0x2D3A28, 1);
                    events = true;
                    break;
                case UseType.POTRERO:
                    box.fill = new SolidColorFill(Color.getColorCode(_pasalePlayer.color), 1);
                    events = true;
                    break;
                case UseType.SILVOPASTORAL:
                    box.fill = new SolidColorFill(Color.getColorCode(_pasalePlayer.color), 0.4);
                    events = true;
                    break;               
                default:
                    box.fill = new SolidColorFill(Color.getColorCode(Color.BLACK), 1);
                    break;
            }

            if (events)
                box.addEventListener(MouseEvent.CLICK, choose);

            return box;
        }

        private function hasPathToWater (box:PasaleBox):Boolean {
            var ret:Boolean = false;
            

            return ret;
        }
    }
}