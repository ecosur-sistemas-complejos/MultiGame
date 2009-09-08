package mx.ecosur.multigame.model
{
	import mx.ecosur.multigame.entity.Move;
	
	[RemoteClass (alias="mx.ecosur.multigame.model.Move")]
	public class MoveModel
	{
		private var _move:Move;
		
		public function MoveModel () {
			super();
		}
		
		public function get implementation ():Move {
			return _move;
		}
		
		public function set implementation (move:Move):void {
			_move = move;
		}
		
		public function toString():String {
			return new String ("MoveModel.  Implementation:  " + this.implementation);
		}
	}
}