package mx.ecosur.multigame.model
{
	import mx.ecosur.multigame.manantiales.entity.CheckCondition;
	
	[RemoteClass (alias="mx.ecosur.multigame.model.Condition")]
	public class ConditionModel
	{
		private var _condition:CheckCondition;
		
		public function ConditionModel()
		{
			super ();		
		}
		
		public function get implementation():CheckCondition {
			return _condition;
		}
		
		public function set implementation(condition:CheckCondition):void {
			_condition = condition;
		}
	}
}