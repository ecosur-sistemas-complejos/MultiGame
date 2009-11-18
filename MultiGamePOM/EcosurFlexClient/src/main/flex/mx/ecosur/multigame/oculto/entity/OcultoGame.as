package mx.ecosur.multigame.oculto.entity
{
	import mx.collections.ArrayCollection;
	import mx.ecosur.multigame.entity.Game;

    [RemoteClass (alias=
        "mx.ecosur.multigame.impl.entity.oculto.OcultoGame")]                       
	public class OcultoGame extends Game
	{
		private var _mode:String;
		
		private var _checkConditions:ArrayCollection;		
		
		public function get checkConditions():ArrayCollection {
			return _checkConditions;
		}
		
		public function set checkConditions(conditions:ArrayCollection):void 
		{
		  _checkConditions = conditions;	
		}
		
		public function addCheckCondition (violation:CheckCondition):void {
			if (_checkConditions == null)
			     _checkConditions = new ArrayCollection();
			_checkConditions.addItem(violation); 
		}
	}
}