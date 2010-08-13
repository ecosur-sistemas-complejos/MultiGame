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

    import mx.controls.Alert;
    import mx.ecosur.multigame.component.AbstractBoard;
    import mx.ecosur.multigame.entity.GameGrid;
    import mx.ecosur.multigame.entity.GamePlayer;
    import mx.ecosur.multigame.enum.Color;
    import mx.ecosur.multigame.pasale.entity.PasalePlayer;
    import mx.effects.Fade;
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
        private var _box:IsoBox;

        /* Tile sizes for different conditions */
        private var river:int = 10;
        private var riparian:int = 15;
        private var forest:int = 30;
        private var mountain:int = 55;

        public function PasaleBoard() {
            super();
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
        }

        public function remove(event:ProxyEvent):void {
            var box:IsoBox = IsoBox(event.target);
            _scene.removeChild(box);

        }

        public function choose(event:ProxyEvent):void {
            if (_alert != null) {
                return;
            }
            /* Highlight the square */
            _box = IsoBox(event.target);
            _box.height = _box.height + mountain;
            _box.render(true);
            
            _alert = new ColonizerAlert();
            PopUpManager.addPopUp(_alert, this, true);
            PopUpManager.centerPopUp(_alert);
        }

        private function cancelAlert():void {
            PopUpManager.removePopUp(_alert);            
            _box.height = _box.height - mountain;
            _box.render(true);
            _box = null;
            _alert = null;
        }

        public function colonize(event:DynamicEvent):void {
            Alert.show ("Received dynamic event: [" + event + "]");
            _box.height = _box.height - mountain;
            _box.render(true);
            _box = null;
            PopUpManager.removePopUp(_alert);
            _alert = null;
        }

        override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void
        {
            var pt:Pt = new Pt(width /2, height / 2);
            IsoMath.screenToIso(pt);

            var factor:int = 2;
            var size:int = (unscaledWidth / DIMENSION)/factor;
            var particle:int = size / 2;

            if (_scene != null)
                _scene.removeAllChildren();
            else
                _scene = new IsoScene();
            
            _scene.hostContainer = this;

            /** TODO:  Fix THIS CONSTANT USE, should use nRows and nCols */
            for (var i:int = 0; i < DIMENSION; i++) {
                for (var j:int = 0; j < DIMENSION; j++) {
                    var events:Boolean = true;
                    var box:IsoBox = new IsoBox();
                    /* Location to size boxes based on proximity to water */
                    if (i % 2 == 0 || j % 2 == 0) {
                        if (i == DIMENSION /2 || j == DIMENSION / 2) {
                            box.setSize(size,size, river);
                            box.fill = new SolidColorFill(Color.getColorCode(Color.BLUE), 0.78);
                            events == false;
                        } else if (i == DIMENSION /2 - 1 || i == DIMENSION /2 + 1 || j == DIMENSION /2 - 1 || j == DIMENSION /2 + 1) {
                            box.setSize(size,size,riparian);
                            box.fill = new SolidColorFill(Color.getColorCode(Color.GREEN), 1);
                        } else if (i == DIMENSION /2 - 2 || i == DIMENSION /2 + 2 || j == DIMENSION /2 - 2 || j == DIMENSION /2 + 2) {
                            box.setSize(size,size,forest);
                            box.fill = new SolidColorFill(Color.getColorCode(Color.GREEN), 1);
                        } else if (i == DIMENSION /2 - 3 || i == DIMENSION /2 + 3 || j == DIMENSION /2 - 3 || j == DIMENSION /2 + 3) {
                            box.setSize(size,size,forest);
                            box.fill = new SolidColorFill(Color.getColorCode(Color.GREEN), 1);
                        } else {
                            box.setSize(size,size,mountain);
                            box.fill = new SolidColorFill(Color.getColorCode(Color.GREEN), 1);
                        }

                        if (events)
                            box.addEventListener(MouseEvent.CLICK, choose);

                    } else {
                        if (i == DIMENSION /2 || j == DIMENSION / 2) {
                            box.setSize(size,size, river);
                            box.fill = new SolidColorFill(Color.getColorCode(Color.BLUE), 0.78);
                            events = false;                            
                        }

                        else {
                            box.setSize(size,size, river);
                            box.fill = new SolidColorFill(0XAA9C82, 0.78);
                        }
                    }

                    box.moveTo(i * size + pt.x, j * size + pt.y, (height / 3));
                    _scene.addChild(box);
                }
            }


            _scene.render();   
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
        }
    }
}