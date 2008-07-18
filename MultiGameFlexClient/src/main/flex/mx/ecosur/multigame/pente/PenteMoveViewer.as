package mx.ecosur.multigame.pente {
	
	import mx.collections.ArrayCollection;
	import mx.containers.Accordion;
	import mx.containers.Panel;
	import mx.controls.Button;
	import mx.ecosur.multigame.entity.Cell;
	import mx.ecosur.multigame.helper.Color;
	import mx.ecosur.multigame.pente.entity.PenteMove;
	
	/**
	 * Visual component showing all the moves made in the game
	 * and allowing navegation to different points in the game.
	 */
	public class PenteMoveViewer extends Panel {
		
		private var _moves:Accordion; 
		
		/**
		 * Default constructor 
		 */
		public function PenteMoveViewer(){
			super();
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
			
			_moves.selectedIndex = i - 1;
		}
		
		/**
		 * Adds a move to the accordion of moves.
		 *  
		 * @param move the move to add.
		 */
		public function addMove(move:PenteMove):void{
			
			//create and add the move information
			var pmi:PenteMoveInfo = new PenteMoveInfo();
			_moves.addChild(pmi);
			pmi.penteMove = move;
			
			//create button header
			var btn:Button = _moves.getHeaderAt(_moves.getChildIndex(pmi));
			btn.label = " to " + getCellDescription(move.destination);
			btn.setStyle("icon", Color.getCellIcon(move.player.color));
			btn.setStyle("paddingBottom", 5);
			btn.setStyle("paddingTop", 5);
			
			_moves.selectedChild = pmi;
		}
		
		private function getCellDescription(cell:Cell):String{
			return "row " + cell.row + ", column " + cell.column;
		}
		
		override protected function createChildren():void{
			super.createChildren();
			_moves = new Accordion();
			_moves.percentWidth = 100; 
			addChild(_moves);
		} 
		
	}
		
}