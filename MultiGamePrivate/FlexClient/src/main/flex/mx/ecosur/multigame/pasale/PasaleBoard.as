/*
* Copyright (C) 2010 ECOSUR, Andrew Waterman
* 
* Licensed under the Academic Free License v. 3.2. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
*/

package mx.ecosur.multigame.pasale
{
    import mx.ecosur.multigame.component.AbstractBoard;
    import mx.ecosur.multigame.entity.GamePlayer;
    import mx.ecosur.multigame.entity.GameGrid;

    import as3isolib.display.primitive.IsoBox;
    import as3isolib.display.scene.IsoScene;

    public class PasaleBoard extends AbstractBoard {

        protected var _currentPlayer:GamePlayer;
        protected var _grid:GameGrid;

        public function PasaleBoard (grid:GameGrid) {
            this();
            _grid = grid;
        }

        public function set currentPlayer (currentPlayer:GamePlayer):void {
            this._currentPlayer = currentPlayer;
        }

        override protected function createChildren():void {
            super.createChildren();
            var box0:IsoBox = new IsoBox();
            box0.setSize(25, 25, 25);
            box0.moveTo(200, 0, 0);

            var box1:IsoBox = new IsoBox();
            box1.width = 10;
            box1.length = 25;
            box1.height = 50;
            box1.moveTo(230, -15, 20);

            var box2:IsoBox = new IsoBox();
            box2.setSize(10, 50, 5);
            box2.moveTo(200, 30, 10);

            var scene:IsoScene = new IsoScene();
            scene.hostContainer = this;
            scene.addChild(box2);
            scene.addChild(box1);
            scene.addChild(box0);
            scene.render();
        }
    }
}