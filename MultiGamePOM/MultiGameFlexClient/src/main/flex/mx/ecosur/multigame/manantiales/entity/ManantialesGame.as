package mx.ecosur.multigame.manantiales.entity
{
	import mx.collections.ArrayCollection;
	import mx.ecosur.multigame.entity.Game;
	import mx.ecosur.multigame.manantiales.CheckCondition;

    [RemoteClass (alias=
        "mx.ecosur.multigame.ejb.entity.manantiales.ManantialesGame")]                       
	public class ManantialesGame extends Game
	{
		private var _mode:String;
		
		private var _checkConstraints:ArrayCollection;
		
		public function get mode():String {
			return _mode;
		}
		
		public function set mode(mode:String):void {
			_mode = mode;
		}		
		
		public function get checkConstraints():ArrayCollection {
			return _checkConstraints;
		}
		
		public function set checkConstraints(checkConstraints:ArrayCollection):void 
		{
		  _checkConstraints = checkConstraints;	
		}
		
		public function addCheckConstraint (violation:CheckCondition):void {
			if (_checkConstraints == null)
			     _checkConstraints = new ArrayCollection();
			_checkConstraints.addItem(violation); 
		}
	}
}