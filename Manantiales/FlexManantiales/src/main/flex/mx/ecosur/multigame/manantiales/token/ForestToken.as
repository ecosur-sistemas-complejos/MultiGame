package mx.ecosur.multigame.manantiales.token
{
    import mx.controls.Image;
    import mx.ecosur.multigame.enum.Color;
    import mx.ecosur.multigame.enum.manantiales.TokenType;
import mx.resources.IResourceManager;
import mx.resources.ResourceManager;

[ResourceBundle("Manantiales")]
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