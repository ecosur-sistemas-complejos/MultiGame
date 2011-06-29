package mx.ecosur.multigame.manantiales
{
    import mx.collections.ArrayCollection;
    import mx.containers.Accordion;
    import mx.containers.Panel;
    import mx.controls.Button;
    import mx.ecosur.multigame.enum.Color;
    import mx.ecosur.multigame.entity.manantiales.Suggestion;

    public class SuggestionViewer extends Panel
    {
        private var _suggestions:Accordion;
        private var _selectedSuggestion:SuggestionInfo;
        private var _board:ManantialesBoard;
        private var _handler:SuggestionHandler;
        
        public function SuggestionViewer()
        {
            super();
            _suggestions = new Accordion();
        }
        
        public function get length ():int {
            return _suggestions.numChildren;
        }
        
        public function set handler (handler:SuggestionHandler):void {
            _handler = handler;
        }
        
        public function set board(board:ManantialesBoard):void{
            _board = ManantialesBoard(board);
        }
        
        public function set selectedSuggestion(suggestion:Suggestion):void{
            var si:SuggestionInfo;
            for (var i:int = 0; i < _suggestions.numChildren; i++){
                si = SuggestionInfo(_suggestions.getChildAt(i));
                if (suggestion != null && suggestion.id == si.suggestion.id){
                    
                    //deselect currently selected button
                    var btn:Button;
                    if(_selectedSuggestion){
                        btn = _suggestions.getHeaderAt(_suggestions.getChildIndex(_selectedSuggestion));
                        btn.setStyle("fillColors", [0xE6EEEE, 0xFFFFFF]);
                        btn.setStyle("fillAlphas", [0.6, 0.4]);
                        btn.setStyle("color", 0x0B333C);
                        btn.setStyle("borderColor", 0xAAB3B3);
                    }
                    
                    //select new button
                    btn = _suggestions.getHeaderAt(_suggestions.getChildIndex(si));
                    btn.setStyle("fillColors", [0xFF3300, 0xFF6600]);
                    btn.setStyle("fillAlphas", [1, 1]);
                    btn.setStyle("color", 0xFFFFFF);
                    btn.setStyle("borderColor", 0xFF3300);
                    
                    _selectedSuggestion = si;
                    _suggestions.selectedChild = _selectedSuggestion;
                    return;
                }
            }
        }

        /**
         * Initializes the component from an array of Suggestion entites.
         *  
         * @param suggestions the suggestions to initialize the accordian with.
         */
        public function initFromSuggestions(suggestions:ArrayCollection):void{
            if (suggestions != null) {
                _suggestions.removeAllChildren();
                for (var i:int = 0; i < suggestions.length; i++){
                    var suggestion:Suggestion = Suggestion (suggestions [ i ]);
                    if (suggestion.status == "EVALUATED")
                        addSuggestion (Suggestion (suggestions[i]));
                }
                if(suggestions.length > 0){
                    this.selectedSuggestion = Suggestion(_suggestions[suggestions.length - 1]);
                }
            }
        }
        
        /**
         * Adds a suggestion to the accordion of suggestions.
         *  
         * @param move the suggestion to add.
         */
        public function addSuggestion(suggestion:Suggestion):void{
            
            //create and add the suggestion information
            var si:SuggestionInfo = new SuggestionInfo();
            _suggestions.addChild(si);
            si.suggestion = suggestion;
            
            //create button header
            var btn:Button = _suggestions.getHeaderAt(_suggestions.getChildIndex(si));
            btn.label = " from " + _board.getCellDescription (suggestion.move.currentCell.column, suggestion.move.currentCell.row) + 
                    " to " + _board.getCellDescription(suggestion.move.destinationCell.column, suggestion.move.destinationCell.row);
            
            btn.setStyle("icon", Color.getCellIcon(suggestion.move.player.color));
            btn.setStyle("paddingBottom", 5);
            btn.setStyle("paddingTop", 5);
        }
        
        public function removeSuggestion (suggestion:Suggestion):void {
            var si:SuggestionInfo;
            for (var i:int = 0; i < _suggestions.numChildren; i++){
                si = SuggestionInfo(_suggestions.getChildAt(i));
                if (suggestion != null && suggestion.id == si.suggestion.id){
                    _suggestions.removeChild(si);
                    break;
                }
            }
        }
        
        override protected function measure():void{
            measuredHeight = unscaledHeight;
        }
        
        override protected function createChildren():void{
            super.createChildren();
            
            //create suggestions
            _suggestions = new Accordion();
            _suggestions.percentWidth = 100; 
            addChild(_suggestions);
        }
    }
}