package mx.ecosur.multigame.manantiales.entity {

    import mx.ecosur.multigame.entity.GamePlayer;

    [RemoteClass (alias=
        "mx.ecosur.multigame.manantiales.entity.ManantialesPlayer")]
    public class ManantialesPlayer extends GamePlayer
    {
        private var _score:int, _premiums:int, _forested:int, _moderate:int, _intensive:int,
          _vivero:int, _silvo:int;

        private var _badYear:Boolean;

        public function ManantialesPlayer()
        {
            super();
            _badYear = false;
        }

        public function get score():int {
            return _score;
        }

        public function set score(score:int):void {
            this._score = score;
        }

        public function get forested():int {
            return _forested;
        }

        public function set forested(forested:int):void {
            _forested = forested;
        }

        public function get moderate():int {
            return _moderate;
        }

        public function set moderate(moderate:int):void {
            _moderate = moderate;
        }

        public function get intensive():int {
            return _intensive;
        }

        public function set intensive(intensive:int):void {
            _intensive = intensive;
        }

        public function get vivero():int {
            return _vivero;
        }

        public function set vivero(vivero:int):void {
            _vivero = vivero;
        }

        public function get silvo():int {
            return _silvo;
        }

        public function set silvo(silvo:int):void {
            _silvo = silvo;
        }

        public function get tokens():int {
            return forested + moderate + intensive + vivero + silvo;
        }

        public function get premiums():int {
            return _premiums;
        }

        public function set premiums (prems:int):void {
            _premiums = prems;
        }
    }
}
