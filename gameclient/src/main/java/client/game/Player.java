package client.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.exception.PlayerColorFormatException;



public abstract class Player {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public enum Color {
        WHITE((byte) 0),
        BLACK((byte) 1);

        private byte code;

        Color(byte code) {
            this.code = code;
        }

        public byte code() { return code; }

        public Player.Color other() {
            return (this == WHITE) ? BLACK : WHITE;
        }

        public static Color parseByte(byte code)
            throws PlayerColorFormatException {

            if      (code == 0) { return WHITE; }
            else if (code == 1) { return BLACK; }
            else {
                throw new PlayerColorFormatException(Byte.toString(code), "Number is not a defined color code!");
            }
        }

        public static Color parseString(String str)
            throws PlayerColorFormatException {

            try {
                byte code = Byte.parseByte(str);
                return parseByte(code);
            }
            catch (NumberFormatException nfe) {
                throw new PlayerColorFormatException(str, "Can not parse string to number!");
            }
        }
    }
}
