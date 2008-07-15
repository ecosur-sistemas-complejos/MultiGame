package mx.ecosur.multigame.entity {
	
	import mx.collections.ArrayCollection;
	
	/**
	 * Represents a server side GameGrid object.
	 */
	[RemoteClass (alias="mx.ecosur.multigame.GameGrid")]
	public class GameGrid {
		
		private var _cells:ArrayCollection;
		
		public function GameGrid(){
			super();
		}
		
		public function get cells():ArrayCollection{
			return _cells;
		}
		
		public function set cells(cells:ArrayCollection):void {
			_cells = cells;
		}
	}
}