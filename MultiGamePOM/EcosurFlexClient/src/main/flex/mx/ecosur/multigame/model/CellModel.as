package mx.ecosur.multigame.model
{
    import mx.ecosur.multigame.entity.Cell;

    [RemoteClass (alias="mx.ecosur.multigame.model.Cell")]
	public class CellModel
	{
        private var _id:int;
        private var _row:int;
        private var _column:int;
        private var _cell:Cell;

        public function CellModel() {
            super();
        }

        public function get id ():int {
            return _id;
        }

        public function set id (id:int):void {
            _id = id;
        }

        public function get row ():int {
            return _row;
        }

        public function set row (row:int):void {
            _row = row;
        }

        public function get column ():int {
            return _column;
        }

        public function set column (column:int):void {
            _column = column;
        }

        public function get implementation ():Cell {
            return _cell;
        }

        public function set implementation (cell:Cell):void {
            _cell = cell;
        }
    }
}