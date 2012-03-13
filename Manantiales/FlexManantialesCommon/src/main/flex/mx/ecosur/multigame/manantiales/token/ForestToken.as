package mx.ecosur.multigame.manantiales.token
{
    import mx.ecosur.multigame.manantiales.enum.TokenType;

    [ResourceBundle("ManantialesCommon")]
    public class ForestToken extends ManantialesToken
    {
        public function ForestToken () {
            super ();
            _tooltip = resourceManager.getString("Manantiales", "manantiales.token.forest");
            _label = "F";
            _type = TokenType.FOREST;
        }
    }
}
