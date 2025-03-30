package cz.radovanmoncek.test.ship.bootstrap;

import cz.radovanmoncek.ship.bootstrap.NettgameClientBootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.*;

public class NettgameClientBootstrapTest {
    private static NettgameClientBootstrap nettgameClientBootstrap;

    @BeforeAll
    static void setup() {

        nettgameClientBootstrap = NettgameClientBootstrap.returnNewInstance();
    }

    @Test
    void singletonTest() {

        assertEquals(NettgameClientBootstrap.returnNewInstance(), nettgameClientBootstrap);
    }

    @Test
    void withPortTest() throws NoSuchFieldException, IllegalAccessException {

        nettgameClientBootstrap.setGameServerPort(4321);

        assertEquals(4321, returnEncapsulatedField(nettgameClientBootstrap.getClass(), "gameServerPort").get(nettgameClientBootstrap));

        assertThrows(IllegalArgumentException.class, () -> nettgameClientBootstrap.setGameServerPort(-1));
        assertThrows(IllegalArgumentException.class, () -> nettgameClientBootstrap.setGameServerPort(21));
        assertThrows(IllegalArgumentException.class, () -> nettgameClientBootstrap.setGameServerPort(53));
        assertThrows(IllegalArgumentException.class, () -> nettgameClientBootstrap.setGameServerPort(443));
        assertThrows(IllegalArgumentException.class, () -> nettgameClientBootstrap.setGameServerPort(1024));
        assertThrows(IllegalArgumentException.class, () -> nettgameClientBootstrap.setGameServerPort(65536));

        assertDoesNotThrow(() -> nettgameClientBootstrap.setGameServerPort(54321));
    }

    @Test
    void withServerAddressTest() throws NoSuchFieldException, IllegalAccessException {

        final var address = InetAddress.getLoopbackAddress();

        nettgameClientBootstrap.setGameServerAddress(address);

        assertEquals(address, returnEncapsulatedField(nettgameClientBootstrap.getClass(), "gameServerAddress").get(nettgameClientBootstrap));
    }

    private static Field returnEncapsulatedField(final Class<?> clazz, final String fieldName) throws NoSuchFieldException { //todo: ReflectionUtilities

        final var field = clazz.getDeclaredField(fieldName);

        field.setAccessible(true);

        return field;
    }
}
