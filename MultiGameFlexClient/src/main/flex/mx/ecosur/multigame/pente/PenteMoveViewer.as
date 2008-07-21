package mx.ecosur.multigame.pente {
	
	import flash.events.MouseEvent;
	
	import mx.collections.ArrayCollection;
	import mx.containers.Accordion;
	import mx.containers.ControlBar;
	import mx.containers.Panel;
	import mx.controls.Button;
	import mx.controls.Spacer;
	import mx.ecosur.multigame.enum.Color;
	import mx.events.DynamicEvent;
	
	import mx.ecosur.multigame.pente.entity.*;
	import mx.ecosur.multigame.entity.*;
	
	/**
	 * Visual component showing all the moves made in the game
	 * and allowing navegation to different points in the game.
	 */
	public class PenteMoveViewer extends Panel {
		
		private var _moves:Accordion; 
		private var _controlBar:ControlBar;
		private var _selectedMove:PenteMoveInfo;
		
		// Define navigation events that this component dispatches
		public static const MOVE_EVENT_GOTO_MOVE:String = "gotoMove";
		[Event(name = MOVE_EVENT_GOTO_MOVE, type = "mx.events.DynamicEvent")]
		
		/**
		 * Default constructor 
		 */
		public function PenteMoveViewer(){
			super();
		}
		
		public function set selectedMove(move:PenteMove):void{
			var pmi:PenteMoveInfo;
			for (var i:int = 0; i < _moves.numChildren; i++){
				pmi = PenteMoveInfo(_moves.getChildAt(i))
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
				addMove(PenteMove(moves[i]));					
			}
			if(moves.length > 0){
				this.selectedMove = PenteMove(moves[moves.length - 1]); 
			}
		}
		
		/**
		 * Adds a move to the accordion of moves.
		 *  
		 * @param move the move to add.
		 */
		public function addMove(move:PenteMove):void{
			
			//create and add the move information
			var pmi:PenteMoveInfo = new PenteMoveInfo();
			pmi.addEventListener(PenteMoveInfo.GOTO_MOVE_EVENT, goDirect);
			_moves.addChild(pmi);
			pmi.penteMove = move;
			
			//create button header
			var btn:Button = _moves.getHeaderAt(_moves.getChildIndex(pmi));
			btn.label = " to " + getCellDescription(move.destination);
			btn.setStyle("icon", Color.getCellIcon(move.player.color));
			btn.setStyle("paddingBottom", 5);
			btn.setStyle("paddingTop", 5);
		}
		
		private function getCellDescription(cell:Cell):String{
			//TODO: using the board size in this way is very ugly!
			return "row " + (19 - cell.row) + ", column " + (cell.column + 1);
		}
		
		private function goDirect(event:DynamicEvent):void{
			var moveEvent:DynamicEvent = new DynamicEvent(MOVE_EVENT_GOTO_MOVE);
			moveEvent.move = PenteMoveInfo(event.target).penteMove;
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
			var move:PenteMove = PenteMoveInfo(_moves.getChildAt(ind - 1)).penteMove;
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
			var move:PenteMove = PenteMoveInfo(_moves.getChildAt(ind + 1)).penteMove;
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