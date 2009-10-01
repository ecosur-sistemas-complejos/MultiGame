package mx.ecosur.multigame.model
{
	import mx.ecosur.multigame.entity.Move;
    import mx.ecosur.multigame.enum.MoveStatus;

	
	[RemoteClass (alias="mx.ecosur.multigame.model.Move")]
	public class MoveModel
	{
		private var _move:Move;
		private var _player:GamePlayerModel;
        private var _current:CellModel;
        private var _destination:CellModel;
        private var _status:MoveStatus;

		public function MoveModel () {
			super();
		}

        public function get status ():MoveStatus {
            return _status;
        }

        public function set status (status:MoveStatus):void {
            _status = status;                        
        }

        public function get player ():GamePlayerModel {
            return _player;
        }

        public function set player(player:GamePlayerModel):void {
            _player = player;
        }

        public function get current ():CellModel {
            return _current;
        }

        public function set current (cell:CellModel):void {
            _current = cell;
        }

        public function get destination ():CellModel {
            return _destination;
        }

        public function set destination (cell:CellModel):void {
            _destination = cell;
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