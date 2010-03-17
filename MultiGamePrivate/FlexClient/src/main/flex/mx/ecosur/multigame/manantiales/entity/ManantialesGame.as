//copyright

package mx.ecosur.multigame.manantiales.entity
{
    import mx.collections.ArrayCollection;
    import mx.ecosur.multigame.entity.Game;

    [RemoteClass (alias=
        "mx.ecosur.multigame.impl.entity.manantiales.ManantialesGame")]
    public class ManantialesGame extends Game
    {
        private var _mode:String;

        private var _suggestions:ArrayCollection;

        private var _checkConditions:ArrayCollection;

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
    }
}