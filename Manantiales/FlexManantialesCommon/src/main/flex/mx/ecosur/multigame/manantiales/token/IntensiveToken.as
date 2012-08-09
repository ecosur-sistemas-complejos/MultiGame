package mx.ecosur.multigame.manantiales.token
{
import mx.ecosur.multigame.manantiales.token.ManantialesToken;
import mx.ecosur.multigame.manantiales.enum.TokenType;

    [ResourceBundle("ManantialesCommon")]
    public class IntensiveToken extends ManantialesToken
    {
        public function IntensiveToken()
        {
            super();
            _tooltip = resourceManager.getString("Manantiales", "manantiales.token.intensive");
            _label = "I";
            _type = TokenType.INTENSIVE;
        }
    }
}
