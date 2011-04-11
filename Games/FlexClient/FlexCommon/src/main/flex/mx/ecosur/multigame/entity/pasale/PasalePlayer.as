package mx.ecosur.multigame.entity.pasale
{
    import mx.ecosur.multigame.entity.GamePlayer;

    [RemoteClass (alias="mx.ecosur.multigame.impl.entity.pasale.PasalePlayer")]
    public class PasalePlayer extends GamePlayer
    {

        private var _points:int, _cheatYears:int;

        public function PasalePlayer() {
            super();
        }

        public function get cheatYears():int {
            return _cheatYears;
        }

        public function set cheatYears(cheatYears:int):void {
            _cheatYears = cheatYears;
        }

        public function get score():int {
            return _points;
        }

        public function set score (score:int):void {
            _points = score;
        }
    }
}