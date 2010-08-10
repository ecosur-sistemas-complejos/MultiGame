package mx.ecosur.multigame.pasale.entity
{
    import mx.ecosur.multigame.entity.GamePlayer;

    [RemoteClass (alias=
        "mx.ecosur.multigame.impl.entity.pasale.PasalePlayer")]
    public class PasalePlayer extends GamePlayer
    {

        private var _points:int, _cheatYears:int;

        public function PasalePlayer() {
            super();
        }

        public function get cheatYears():int {
            return _cheatYears;
        }

        public function setCheatYears(cheatYears:int):void {
            _cheatYears = cheatYears;
        }

        public function get score():int {
            return _points;
        }

        public function setScore (score:int):void {
            _points = score;
        }

        public void reset() {
            _cheatYears = 0;
            _points = 0;
        }
    }
}