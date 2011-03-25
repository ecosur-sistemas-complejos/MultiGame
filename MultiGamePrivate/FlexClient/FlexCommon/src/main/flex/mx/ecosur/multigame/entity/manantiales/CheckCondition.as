package mx.ecosur.multigame.entity.manantiales
{
    import mx.collections.ArrayCollection;
    import mx.ecosur.multigame.entity.GamePlayer;

    [RemoteClass (alias="mx.ecosur.multigame.impl.entity.manantiales.CheckCondition")]
    public class CheckCondition
    {
        private var _reason:String;
        private var _violators:ArrayCollection
        private var _player:GamePlayer;
        private var _expired:Boolean;

        public function CheckCondition () {
            super();
        }

        public function get expired():Boolean {
            return _expired;
        }

        public function set expired(expired:Boolean):void {
            _expired = expired;
        }

        public function get player():GamePlayer {
            return _player;
        }

        public function set player(player:GamePlayer):void {
            _player = player;
        }

        public function set reason (reason:String):void {
            _reason = reason;
        }

        public function get reason ():String {
            return _reason;
        }

        public function set violators (violators:ArrayCollection):void {
            _violators = violators;
        }

        public function get violators ():ArrayCollection {
            return _violators;
        }

        public function toString():String {
            return _player.registrant.name + " initiated the check '"
             + _reason +"' which must be resolved before his/her next turn.";
        }
    }
}