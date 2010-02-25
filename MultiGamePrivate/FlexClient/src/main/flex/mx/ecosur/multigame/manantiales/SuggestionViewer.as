/*
 * Copyright (C) 2010 ECOSUR, Andrew Waterman
 * Licensed under the Academic Free License v. 3.2.
 * http://www.opensource.org/licenses/afl-3.0.php
 *
 */

package mx.ecosur.multigame.manantiales
{
    import mx.containers.Accordion;
    import mx.containers.Panel;
    import mx.controls.Alert;
    import mx.controls.Button;
    import mx.ecosur.multigame.component.AbstractBoard;
    import mx.ecosur.multigame.enum.Color;
    import mx.ecosur.multigame.manantiales.entity.Suggestion;

    public class SuggestionViewer extends Panel {

        private var _suggestions:Accordion;

        /**
         * Default constructor
         */
        public function SuggestionViewer(){
            super();
        }


        private var _board:AbstractBoard;
                
        public function set board(board:AbstractBoard):void{
            _board = board;
        }

        override protected function createChildren():void{
            Alert.show("Calling super.createchildren");
            super.createChildren();
            Alert.show("Creating children ...");

            //create moves
            _suggestions = new Accordion();
            _suggestions.percentWidth = 100;
            addChild(_suggestions);
        }

        /**
         * Adds a suggestion to the accordion of suggestions.
         *
         * @param suggestion the suggestion to add.
         */
        public function addSuggestion(suggestion:Suggestion):void{

            Alert.show("Adding suggestion " + suggestion.toString());

            //create and add the move information
            var si:SuggestionInfo = new SuggestionInfo();
            _suggestions.addChild(si);
            si.suggestion = suggestion;

            //create button header
            var btn:Button = _suggestions.getHeaderAt(_suggestions.getChildIndex(si));
            btn.label = " to " + suggestion.move.destinationCell.toString();
            //btn.label = " to " + _board.getCellDescription(suggestion.move.destinationCell.column, suggestion.move.destinationCell.row);
            btn.setStyle("icon", Color.getCellIcon(suggestion.move.player.color));
            btn.setStyle("paddingBottom", 5);
            btn.setStyle("paddingTop", 5);
        }

        override protected function measure():void{
            measuredHeight = unscaledHeight;
        }
        
    }
}
