package mx.ecosur.multigame.manantiales.entity
{
	import mx.ecosur.multigame.entity.Move;

    [RemoteClass (alias=
        "mx.ecosur.multigame.ejb.entity.manantiales.ManantialesMove")]
	public class ManantialesMove extends Move
	{
		private var _badYear:Boolean;
		
		public function ManantialesMove()
		{
			super();
			_badYear = false;
		}
		
		public function get badYear():Boolean {
			return _badYear;
		}
		
		public function set badYear (year:Boolean):void {
			_badYear = year;
		}
	}
}