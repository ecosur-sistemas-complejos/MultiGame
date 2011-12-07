package mx.ecosur.multigame.entity.manantiales
{
    import mx.collections.ArrayCollection;
    import mx.ecosur.multigame.entity.GamePlayer;
import mx.resources.ResourceManager;

[ResourceBundle("Commons")]
    [RemoteClass (alias="mx.ecosur.multigame.manantiales.entity.CheckCondition")]
    public class CheckCondition
    {
        private var _reason:String;
        private var _violators:ArrayCollection
        private var _player:GamePlayer;
        private var _expired:Boolean;
        private var resourceManager:ResourceManager;

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

        public function get reason ():String {
            return _reason;
        }

        public function set reason(reason:String):void {
            _reason = reason;
        }

        public function get type():String {
            return _reason;
        }

        public function set type(typ:String):void {
            _reason = typ;
        }

        public function set violators (violators:ArrayCollection):void {
            _violators = violators;
        }

        public function get violators ():ArrayCollection {
            return _violators;
        }

        public function toString():String {
            return _player.name +  " " + ResourceManager.getInstance().getString("Commons", "check.start") +" " + _reason + " " +
                    ResourceManager.getInstance().getString("Commons", "check.end");
        }
    }
}
