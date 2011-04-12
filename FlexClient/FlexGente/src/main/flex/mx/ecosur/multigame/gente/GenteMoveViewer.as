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
    
    import flash.events.MouseEvent;
    
    import mx.collections.ArrayCollection;
    import mx.containers.Accordion;
    import mx.containers.ControlBar;
    import mx.containers.Panel;
    import mx.controls.Button;
    import mx.controls.Spacer;
    import mx.ecosur.multigame.entity.*;
    import mx.ecosur.multigame.enum.Color;
    import mx.ecosur.multigame.gente.entity.*;
    import mx.events.DynamicEvent;
    
    /**
     * Visual component showing all the moves made in the game
     * and allowing navegation to different points in the game.
     */
    [ResourceBundle("Commons")]
    public class GenteMoveViewer extends Panel {
        
        private var _moves:Accordion; 
        private var _controlBar:ControlBar;
        private var _selectedMove:GenteMoveInfo;
        private var _board:GenteBoard;
        
        // Define navigation events that this component dispatches
        public static const MOVE_EVENT_GOTO_MOVE:String = "gotoMove";
        [Event(name = MOVE_EVENT_GOTO_MOVE, type = "mx.events.DynamicEvent")]
        
        /**
         * Default constructor 
         */
        public function GenteMoveViewer(){
            super();
        }
        
        public function set board(board:GenteBoard):void{
            _board = board;
        }
        
        public function set selectedMove(move:GenteMove):void{
            var pmi:GenteMoveInfo;
            for (var i:int = 0; i < _moves.numChildren; i++){
                pmi = GenteMoveInfo(_moves.getChildAt(i))
                if (move.id == pmi.penteMove.id){
                    
                    //deselect currently selected button
                    var btn:Button;
                    if(_selectedMove){
                        btn = _moves.getHeaderAt(_moves.getChildIndex(_selectedMove));
                        btn.setStyle("fillColors", [0xE6EEEE, 0xFFFFFF]);
                        btn.setStyle("fillAlphas", [0.6, 0.4]);
                        btn.setStyle("color", 0x0B333C);
                        btn.setStyle("borderColor", 0xAAB3B3);
                    }
                    
                    //select new button
                    btn = _moves.getHeaderAt(_moves.getChildIndex(pmi));
                    btn.setStyle("fillColors", [0xFF3300, 0xFF6600]);
                    btn.setStyle("fillAlphas", [1, 1]);
                    btn.setStyle("color", 0xFFFFFF);
                    btn.setStyle("borderColor", 0xFF3300);
                    
                    _selectedMove = pmi;
                    _moves.selectedChild = _selectedMove;
                    return;
                }
            }
        }
        
        /**
         * Initializes the component from an array of Move entites.
         *  
         * @param moves the moves to initialize the accordian with.
         */
        public function initFromMoves(moves:ArrayCollection):void{
            
            _moves.removeAllChildren();
            for (var i:int = 0; i < moves.length; i++){
                addMove(GenteMove(moves[i]));
            }
            if(moves.length > 0){
                this.selectedMove = GenteMove(moves[moves.length - 1]);
            }
        }
        
        /**
         * Adds a move to the accordion of moves.
         *  
         * @param move the move to add.
         */
        public function addMove(move:GenteMove):void{
            
            //check that move has not already been added
            for (var i:int = 0; i < _moves.numChildren; i++){
                if (GenteMoveInfo(_moves.getChildAt(i)).penteMove.id == move.id){
                    return;
                }
            }
            
            //create and add the move information
            var pmi:GenteMoveInfo = new GenteMoveInfo();
            pmi.addEventListener(GenteMoveInfo.GOTO_MOVE_EVENT, goDirect);
            _moves.addChild(pmi);
            pmi.penteMove = move;
            
            //create button header
            var btn:Button = _moves.getHeaderAt(_moves.getChildIndex(pmi));
            btn.label = resourceManager.getString("Commons", "move.history.to") +
                _board.getCellDescription(move.destinationCell.column, move.destinationCell.row);
            btn.setStyle("icon", Color.getCellIcon(move.player.color));
            btn.setStyle("paddingBottom", 5);
            btn.setStyle("paddingTop", 5);
        }
        
        /**
         * Searches for a move in the accordion with the same id as
         * that passed in and updates it.
         *  
         * @param move the move to update
         * 
         */
        public function updateMove(move:GenteMove):void{
            
            var pmi:GenteMoveInfo;
            for (var i:int = 0; i < _moves.numChildren; i++){
                pmi = GenteMoveInfo(_moves.getChildAt(i));
                if(pmi.penteMove.id == move.id){
                    pmi.penteMove = move;
                    break;
                }
            }
        }
        
        
        private function goDirect(event:DynamicEvent):void{
            var moveEvent:DynamicEvent = new DynamicEvent(MOVE_EVENT_GOTO_MOVE);
            moveEvent.move = GenteMoveInfo(event.target).penteMove;
            dispatchEvent(moveEvent);
        }
        
        private function goBack(event:MouseEvent):void{
            
            //check that a selected move exists
            if (_selectedMove == null){
                return;
            }
            
            //get previous move
            var ind:int = _moves.getChildIndex(_selectedMove);
            if (ind == 0){
                return;
            }
            var move:GenteMove = GenteMoveInfo(_moves.getChildAt(ind - 1)).penteMove;
            var moveEvent:DynamicEvent = new DynamicEvent(MOVE_EVENT_GOTO_MOVE);
            moveEvent.move = move;
            dispatchEvent(moveEvent);
        }
        
        private function goForward(event:MouseEvent):void{
            
            //check that a selected move exists
            if (_selectedMove == null){
                return;
            }
            
            //get next move
            var ind:int = _moves.getChildIndex(_selectedMove);
            if (ind >= _moves.numChildren - 1){
                return;
            }
            var move:GenteMove = GenteMoveInfo(_moves.getChildAt(ind + 1)).penteMove;
            var moveEvent:DynamicEvent = new DynamicEvent(MOVE_EVENT_GOTO_MOVE);
            moveEvent.move = move;
            dispatchEvent(moveEvent);
        }
        
        override protected function measure():void{
            measuredHeight = unscaledHeight;
        }
        
        override protected function createChildren():void{
            super.createChildren();
            
            //create moves
            _moves = new Accordion();
            _moves.percentWidth = 100; 
            addChild(_moves);
            
            //create control bar
            var btnBack:Button = new Button();
            btnBack.label = "< ";
            btnBack.addEventListener(MouseEvent.MOUSE_UP, goBack);
            var btnForward:Button = new Button();
            btnForward.label = "> ";
            var spacer:Spacer = new Spacer();
            spacer.width = 20;
            btnForward.addEventListener(MouseEvent.MOUSE_UP, goForward);
            _controlBar = new ControlBar();
            _controlBar.addChild(spacer);
            _controlBar.addChild(btnBack);
            _controlBar.addChild(btnForward);
            addChild(_controlBar)
            
        } 
        
    }
    
}