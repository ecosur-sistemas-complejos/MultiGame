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

    import as3isolib.display.IsoView;
    import as3isolib.display.scene.IsoScene;
    import as3isolib.geom.IsoMath;
    import as3isolib.geom.Pt;
    import as3isolib.graphics.IFill;
    import as3isolib.graphics.SolidColorFill;
    import eDpLib.events.ProxyEvent;
    import flash.events.Event;
    import flash.events.MouseEvent;
    import flash.filters.GlowFilter;
    import mx.controls.Alert;
    import mx.ecosur.multigame.component.AbstractBoard;
    import mx.ecosur.multigame.entity.GameGrid;
    import mx.ecosur.multigame.entity.GamePlayer;
    import mx.ecosur.multigame.enum.Color;
    import mx.ecosur.multigame.enum.MoveStatus;
    import mx.ecosur.multigame.pasale.entity.PasaleFicha;
    import mx.ecosur.multigame.pasale.entity.PasaleGrid;
    import mx.ecosur.multigame.pasale.entity.PasaleMove;
    import mx.ecosur.multigame.pasale.entity.PasalePlayer;
    import mx.ecosur.multigame.pasale.enum.UseType;
    import mx.effects.Sequence;
    import mx.events.DynamicEvent;
    import mx.managers.PopUpManager;

    public class PasaleBoard extends AbstractBoard {

        private static var DIMENSION:int = 21;

        protected var _pasalePlayer:PasalePlayer;

        protected var _blinkAnim:Sequence;

        private var _alert:ColonizerAlert;

        private var _grid:PasaleGrid;

        private var _scene:IsoScene;

        private var _view:IsoView;

        private var _box:PasaleBox;

        private var _fill:IFill;

        private var _controller:PasaleGameController;

        /* 800 x 600 minium board size */

        private static var WIDTH:int = 800;

        private static var HEIGHT:int = 600;

        private var _viewHeight:int = HEIGHT;

        private var _viewWidth:int = WIDTH;

        /* Tile sizes for different visual conditions */
        private var river:int = 10;

        private var riparian:int = 20;

        private var _size:int = 0;

        private var pan:Boolean;

        private var lastX:int;

        private var lastY:int;

        private var zoomed:Boolean;

        private var zoom:int;

        private var _glow:GlowFilter;

        public function PasaleBoard() {
            super();
            _glow = new GlowFilter( 0xFF8000, 1, 6, 6, 64 ); 
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
            _grid = PasaleGrid(value);
            update();
        }

        public function get viewHeight():int {
            if (measuredHeight == 0)
                measure();
            if (measuredHeight > HEIGHT)
                _viewHeight = measuredHeight;
            else
                _viewHeight = HEIGHT;

            return _viewHeight;
        }

        public function get viewWidth():int {
            if (measuredWidth == 0)
                measure();
            if (measuredWidth > WIDTH)
                _viewWidth = measuredWidth;
            else
                _viewWidth = WIDTH;
            
            return _viewWidth;
        }

        public function remove(event:ProxyEvent):void {
            _scene.removeChild(PasaleBox(event.target));
        }

        public function startPan(event:MouseEvent):void {
            if (!pan) {
                pan = true;
                lastX = event.stageX;
                lastY = event.stageY;
            } else
                pan = false;
        }

        public function panView (event:MouseEvent):void {
            if (pan) {
                _view.panBy(lastX - event.stageX,  lastY - event.stageY);
                lastX = event.stageX;
                lastY = event.stageY;
            }
        }

        public function stopPan (event:Event):void {
            pan = false;
        }

        public function zoomView (event:MouseEvent):void {
            /* Always interrupt panning */
            if (pan)
                pan = false;
            if (zoomed) {
                _view.zoom(1);
                zoomed = false;
            } else {
                _view.zoom(0.5);
                zoomed = true;
            }
        }


        public function choose(event:ProxyEvent):void {
            if (!pan && _controller.ready()) {
                if (_alert != null) {
                    return;
                }

                /* Highlight the square if it's colonizable */
                _box = PasaleBox(event.target);
                if (_box.type == UseType.SOIL_PARTICLE || _box.type == UseType.WATER_PARTICLE)
                    return;
                _box.container.filters = [ _glow ];

                _alert = new ColonizerAlert();

                if (_box.type == UseType.FOREST || _box.type == UseType.UNDEVELOPED)
                    _alert.dev = true;
                else
                    _alert.dev = false;

                if (_controller.ready()) {
                    _fill = _box.fill;
                    _box.fill = new SolidColorFill(Color.getColorCode(currentPlayer.color), 0.2);
                    _box.render();
                    _alert.addEventListener("build", colonize);
                    _alert.addEventListener("cancel", cancelAlert);
                    PopUpManager.addPopUp(_alert, this, true);
                    PopUpManager.centerPopUp(_alert);
                }
            }
        }

        private function cancelAlert(event:DynamicEvent):void {
            PopUpManager.removePopUp(_alert);
            _box.fill = _fill;
            _box.render(true);
            _box = null;
            _fill = null;
            _alert = null;
        }

        public function colonize(event:DynamicEvent):void {
            var targetType:String;

            if (_alert.dev == true)
                targetType = _alert.development.selectedItem.data;
            else if (_alert.dev == false)
                targetType = UseType.FOREST;

            /* clean up */
            _box.fill = _fill;
            _fill = null;
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
                    destination.color = _pasalePlayer.color;
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
            measuredMinWidth = WIDTH;
            measuredMinHeight = HEIGHT;

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

        override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void {
            findSize (viewWidth, viewHeight);
            update();
        }

        public function get size():int {
            return _size;
        }

        public function set size (sze:int):void {
            _size = sze;
        }

        public function findSize(w:int, h:int):int {
            size = (w /DIMENSION);
            return _size;
        }


        public function update ():void
        {
            if (_view) {
                _view.removeAllScenes();
            } else {
                _view = new IsoView();
                zoom = _view.currentZoom;
                addEventListener(MouseEvent.MOUSE_DOWN, startPan);
                addEventListener(MouseEvent.MOUSE_UP, stopPan);
                addEventListener(MouseEvent.MOUSE_MOVE, panView)
                addEventListener(MouseEvent.MOUSE_WHEEL, zoomView);
                addEventListener( Event.ENTER_FRAME, onRender, false, 0, true );
            }

            if (grid == null)
                return;

            if (_scene == null) {
                _scene = new IsoScene();
            } else
                _scene.removeAllChildren();

            var pt:Pt = new Pt(width /2, height / 2);
            IsoMath.screenToIso(pt);

            var particle:int = size / 2;
            var events:Boolean;

            _scene.removeAllChildren();

            for (var i:int=0; i < _grid.cells.length; i++) {
                var cell:PasaleFicha = PasaleFicha (_grid.cells.getItemAt(i));
                var box:PasaleBox = drawCell (cell.column, cell.row, cell.color, cell.type);
                box.moveTo(box.column * size, box.row * size, height/3);
                _scene.addChild(box);
            }

            _scene.render();
            _view.setSize(viewWidth, viewHeight);
            _view.addScene(_scene);
            _view.showBorder = true;
            _view.clipContent = true;
            _view.render();
            addChild(_view);
        }

        private function drawCell (column:int, row:int, color:String, type:String):PasaleBox {
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
                    box.fill = new SolidColorFill(Color.getColorCode(color), 1);
                    events = true;
                    break;
                case UseType.SILVOPASTORAL:
                    box.fill = new SolidColorFill(Color.getColorCode(color), 0.5);
                    events = true;
                    break;
                default:
                    Alert.show("Unknown type requested for a Cell: " + box.type);
                    break;
            }

            if (events) {
                box.addEventListener(MouseEvent.MOUSE_OVER, enterBox);
                box.addEventListener(MouseEvent.MOUSE_OUT, exitBox);
                box.addEventListener(MouseEvent.CLICK, choose);
            }

            return box;
        }

        private function enterBox (event:ProxyEvent):void {
            var HLBox:PasaleBox = PasaleBox(event.target);
            if (hasPathToWater(HLBox) && _controller.ready()) {
                HLBox.container.filters = [ _glow ];
            }
        }

        private function exitBox (event:ProxyEvent):void {
            var HLBox:PasaleBox = PasaleBox(event.target);
            HLBox.container.filters = null;
        }

        private function hasPathToWater (box:PasaleBox):Boolean {
            return _grid.hasPathToWater(_grid.getLocation(box.column, box.row));
        }

        private function onRender(event:Event):void
        {
            _view.render();
        }
    }
}
