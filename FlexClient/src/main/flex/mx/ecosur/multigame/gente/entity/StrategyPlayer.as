/*
* Copyright (C) 2008 ECOSUR, Andrew Waterman and Max Pimm
* 
* Licensed under the Academic Free License v. 3.2. 
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
*/

package mx.ecosur.multigame.gente.entity {
    
    /**
     * Represents a server side StrategyPlayer object
     */
    [RemoteClass (alias="mx.ecosur.multigame.impl.entity.gente.GenteStrategyAgent")]
    public class StrategyPlayer extends GentePlayer {
   
        private var _strategy:String;

        private var _nextMove:GenteMove;

        public function StrategyPlayer(){
            super();
        }
        
        public function get strategy():String{
            return _strategy;
        }
        
        public function set strategy(strategy:String):void {
            _strategy = strategy
        }

        public function get nextMove():GenteMove {
            return _nextMove;
        }

        public function set nextMove(value:GenteMove):void {
            _nextMove = value;
        }
    }
}
