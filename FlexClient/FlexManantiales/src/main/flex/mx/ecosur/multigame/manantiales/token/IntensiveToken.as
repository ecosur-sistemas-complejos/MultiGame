package mx.ecosur.multigame.manantiales.token
{
    import mx.ecosur.multigame.enum.Color;
    import mx.ecosur.multigame.enum.manantiales.TokenType;

    [ResourceBundle("Manantiales")]
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