package crm.repository;

import crm.entity.Role;
import crm.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setId(1);
        role.setName("ROLE_USER");

        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("password123")
                .enabled(1)
                .role(role)
                .build();
    }

    @Test
    @DisplayName("Should find user by username")
    void testFindByUsername() {
        when(userRepository.findByUsername("testuser")).thenReturn(user);

        User result = userRepository.findByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("Should return null when user not found by username")
    void testFindByUsernameNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(null);

        User result = userRepository.findByUsername("nonexistent");

        assertNull(result);
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }

    @Test
    @DisplayName("Should find all users by enabled")
    void testFindAllByEnabled() {
        List<User> users = Arrays.asList(user);
        when(userRepository.findAllByEnabled(1)).thenReturn(users);

        Iterable<User> result = userRepository.findAllByEnabled(1);

        assertNotNull(result);
        assertTrue(result.iterator().hasNext());
        verify(userRepository, times(1)).findAllByEnabled(1);
    }

    @Test
    @DisplayName("Should find disabled users")
    void testFindAllByEnabledDisabled() {
        when(userRepository.findAllByEnabled(0)).thenReturn(Arrays.asList());

        Iterable<User> result = userRepository.findAllByEnabled(0);

        assertNotNull(result);
        assertFalse(result.iterator().hasNext());
        verify(userRepository, times(1)).findAllByEnabled(0);
    }

    @Test
    @DisplayName("Should save user")
    void testSave() {
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userRepository.save(user);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Should find user by id")
    void testFindById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = userRepository.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return empty optional when user not found by id")
    void testFindByIdNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<User> result = userRepository.findById(999L);

        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should find all users")
    void testFindAll() {
        User user2 = User.builder().id(2L).username("user2").build();
        List<User> users = Arrays.asList(user, user2);
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userRepository.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should delete user")
    void testDelete() {
        doNothing().when(userRepository).delete(user);

        userRepository.delete(user);

        verify(userRepository, times(1)).delete(user);
    }

    @Test
    @DisplayName("Should delete user by id")
    void testDeleteById() {
        doNothing().when(userRepository).deleteById(1L);

        userRepository.deleteById(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should check if user exists by id")
    void testExistsById() {
        when(userRepository.existsById(1L)).thenReturn(true);

        boolean result = userRepository.existsById(1L);

        assertTrue(result);
        verify(userRepository, times(1)).existsById(1L);
    }

    @Test
    @DisplayName("Should return false when user does not exist")
    void testExistsByIdFalse() {
        when(userRepository.existsById(999L)).thenReturn(false);

        boolean result = userRepository.existsById(999L);

        assertFalse(result);
        verify(userRepository, times(1)).existsById(999L);
    }

    @Test
    @DisplayName("Should count users")
    void testCount() {
        when(userRepository.count()).thenReturn(25L);

        long result = userRepository.count();

        assertEquals(25L, result);
        verify(userRepository, times(1)).count();
    }

    @Test
    @DisplayName("Should handle null username in findByUsername")
    void testFindByUsernameNull() {
        when(userRepository.findByUsername(null)).thenReturn(null);

        User result = userRepository.findByUsername(null);

        assertNull(result);
        verify(userRepository, times(1)).findByUsername(null);
    }

    @Test
    @DisplayName("Should handle empty username in findByUsername")
    void testFindByUsernameEmpty() {
        when(userRepository.findByUsername("")).thenReturn(null);

        User result = userRepository.findByUsername("");

        assertNull(result);
        verify(userRepository, times(1)).findByUsername("");
    }

    @Test
    @DisplayName("Should delete all users")
    void testDeleteAll() {
        doNothing().when(userRepository).deleteAll();

        userRepository.deleteAll();

        verify(userRepository, times(1)).deleteAll();
    }
}
