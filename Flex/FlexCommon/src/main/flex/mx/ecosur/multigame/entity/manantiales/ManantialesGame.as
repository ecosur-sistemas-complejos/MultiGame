//copyright

package mx.ecosur.multigame.entity.manantiales
{
    import mx.collections.ArrayCollection;
    import mx.ecosur.multigame.entity.Game;
    import mx.ecosur.multigame.entity.manantiales.CheckCondition;
    import mx.ecosur.multigame.entity.manantiales.Suggestion;

    [RemoteClass (alias=
        "mx.ecosur.multigame.impl.entity.manantiales.ManantialesGame")]
    public class ManantialesGame extends Game
    {
        private var _mode:String;

        private var _suggestions:ArrayCollection;

        private var _checkConditions:ArrayCollection;

        private var _maxPlayers:int;

        private var _turns:int;

        private var _elapsedTime:Number;

        public function ManantialesGame () {
            super();
        }

        public function get suggestions():ArrayCollection {
            return _suggestions;
        }

        public function set suggestions(value:ArrayCollection):void {
            _suggestions = value;
        }

        public function get maxPlayers():int {
            return _maxPlayers;
        }

        public function set maxPlayers(value:int):void {
            _maxPlayers = value;
        }

        public function get mode():String {
            return _mode;
        }

        public function set mode(mode:String):void {
            _mode = mode;
        }

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

        public function addSuggestion (suggestion:Suggestion):void {
            if (_suggestions == null)
                _suggestions = new ArrayCollection();
            _suggestions.addItem(suggestion);
        }

        public function getSuggestions():ArrayCollection {
            return _suggestions;
        }

        public function setSuggestions(suggestions:ArrayCollection):void {
            _suggestions = suggestions;
        }

        public function get elapsedTime():Number {
            return _elapsedTime;
        }

        public function set elapsedTime(value:Number):void {
            _elapsedTime = value;
        }

        public function get turns():int {
            return _turns;
        }

        public function set turns(value:int):void {
            _turns = value;
        }
    }
}
