package mx.ecosur.multigame.manantiales
{
	import flash.events.MouseEvent;
    
    import mx.collections.ArrayCollection;
    import mx.containers.Accordion;
    import mx.containers.ControlBar;
    import mx.containers.Panel;
    import mx.controls.Button;
    import mx.controls.Spacer;
    
    import mx.ecosur.multigame.component.AbstractBoard;
    import mx.ecosur.multigame.enum.Color;

import mx.ecosur.multigame.entity.manantiales.Ficha;
import mx.ecosur.multigame.entity.manantiales.ManantialesMove;
    
    import mx.events.DynamicEvent;

    [ResourceBundle("Commons")]
    public class MoveViewer extends Panel
    {
        private var _moves:Accordion; 
        private var _controlBar:ControlBar;
        private var _selectedMove:MoveInfo;
        private var _board:ManantialesBoard;
        
        // Define navigation events that this component dispatches
        public static const MOVE_EVENT_GOTO_MOVE:String = "gotoMove";
        [Event(name = MOVE_EVENT_GOTO_MOVE, type = "mx.events.DynamicEvent")]
        
        /**
         * Default constructor 
         */
        public function MoveViewer(){
            super();
        }
        
        public function set board(board:AbstractBoard):void{
            _board = ManantialesBoard(board);
        }
        
        public function set selectedMove(move:ManantialesMove):void{
            var mi:MoveInfo;
            for (var i:int = 0; i < _moves.numChildren; i++){
                mi = MoveInfo(_moves.getChildAt(i));
                if (move != null && move.id == mi.gameMove.id){
                    
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
                    btn = _moves.getHeaderAt(_moves.getChildIndex(mi));
                    btn.setStyle("fillColors", [0xFF3300, 0xFF6600]);
                    btn.setStyle("fillAlphas", [1, 1]);
                    btn.setStyle("color", 0xFFFFFF);
                    btn.setStyle("borderColor", 0xFF3300);
                    
                    _selectedMove = mi;
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
            _selectedMove = null;
            for (var i:int = 0; i < moves.length; i++){
                addMove(ManantialesMove(moves[i]));                   
            }
            if(moves.length > 0){
                this.selectedMove = ManantialesMove(moves[moves.length - 1]); 
            }
        }
        
        /**
         * Adds a move to the accordion of moves.
         *  
         * @param move the move to add.
         */
        public function addMove(move:ManantialesMove):void{
            
            //create and add the move information
            var mi:MoveInfo = new MoveInfo();
            mi.addEventListener(MoveInfo.GOTO_MOVE_EVENT, goDirect);
            _moves.addChild(mi);
            mi.gameMove = move;                

            var current:Ficha, destination:Ficha;
            if (move.currentCell != null)
                current = Ficha(move.currentCell);

            if (move.destinationCell != null)
                destination = Ficha (move.destinationCell);
            
            //create button header
            var btn:Button = _moves.getHeaderAt(_moves.getChildIndex(mi));
            if (move.badYear) {
                btn.label = resourceManager.getString("Commons", "move.history.badyear");
            } else if (move.currentCell == null) {
                btn.label = destination.typeName + " " + resourceManager.getString("Commons", "move.history.to") + " " +
                        _board.getCellDescription(move.destinationCell.column, move.destinationCell.row);
            } else if (move.currentCell != null && move.destinationCell != null) {
                btn.label = current.typeName + " " + resourceManager.getString("Commons", "move.history.from") + " " +
                        _board.getCellDescription (move.currentCell.column, move.currentCell.row) +
                    " to " + destination.typeName + resourceManager.getString("Commons", "move.history.at") + " " +
                        _board.getCellDescription(move.destinationCell.column, move.destinationCell.row);
            } else if (move.currentCell != null && move.destinationCell == null) {
                btn.label = current.typeName + " " + resourceManager.getString("Commons", "move.history.removed") +
                        resourceManager.getString("Commons", "move.history.at") + " " +
                        _board.getCellDescription (move.currentCell.column, move.currentCell.row);
            }

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
        public function updateMove(move:ManantialesMove):void{
            
            var mi:MoveInfo;
            for (var i:int = 0; i < _moves.numChildren; i++){
                mi = MoveInfo(_moves.getChildAt(i));
                if(mi.gameMove.id == move.id){
                    mi.gameMove = move;
                    break;
                }
            }
        }

        
        private function goDirect(event:DynamicEvent):void{
            var moveEvent:DynamicEvent = new DynamicEvent(MOVE_EVENT_GOTO_MOVE);
            moveEvent.move = MoveInfo(event.target).gameMove;
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
            var move:ManantialesMove = MoveInfo(_moves.getChildAt(ind - 1)).gameMove;
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
            var move:ManantialesMove = MoveInfo(_moves.getChildAt(ind + 1)).gameMove;
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
