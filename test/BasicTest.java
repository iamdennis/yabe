import models.Comment;
import models.Post;
import models.User;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

import java.util.List;

public class BasicTest extends UnitTest {

    @Before
    public void setup() {
        Fixtures.deleteDatabase();
    }

    @Test
    public void createPost() {
        // Create a new user and save it
        User test = new User("test@gmail.com", "test", "test").save();

        // Create a new post
        new Post(test, "My first post", "Hello world").save();

        // Test that the post has been created
        assertEquals(1, Post.count());

        // Retrieve all posts created by Bob
        List<Post> testPosts = Post.find("byAuthor", test).fetch();

        // Tests
        assertEquals(1, testPosts.size());
        Post firstPost = testPosts.get(0);
        assertNotNull(firstPost);
        assertEquals(test, firstPost.author);
        assertEquals("My first post", firstPost.title);
        assertEquals("Hello world", firstPost.content);
        assertNotNull(firstPost.postedAt);
    }

    @Test
    public void postComments() {
        // Create a new user and save it
        User test = new User("test@gmail.com", "test", "test").save();

        // Create a new post
        Post testPost = new Post(test, "My first post", "Hello world").save();

        // Post a first comment
        new Comment(testPost, "Jeff", "Nice post").save();
        new Comment(testPost, "Tom", "I knew that !").save();

        // Retrieve all comments
        List<Comment> testPostComments = Comment.find("byPost", testPost).fetch();

        // Tests
        assertEquals(2, testPostComments.size());

        Comment firstComment = testPostComments.get(0);
        assertNotNull(firstComment);
        assertEquals("Jeff", firstComment.author);
        assertEquals("Nice post", firstComment.content);
        assertNotNull(firstComment.postedAt);

        Comment secondComment = testPostComments.get(1);
        assertNotNull(secondComment);
        assertEquals("Tom", secondComment.author);
        assertEquals("I knew that !", secondComment.content);
        assertNotNull(secondComment.postedAt);
    }

    @Test
    public void createAndRetrieveUser() {
        // Create a new user and save it
        new User("test@gmail.com", "test", "test").save();

        // Retrieve the user with e-mail address bob@gmail.com
        User test = User.find("byEmail", "test@gmail.com").first();

        // Test
        assertNotNull(test);
        assertEquals("test", test.fullName);
    }

    @Test
    public void tryConnectAsUser() {
        // Create a new user and save it
        new User("test@gmail.com", "test", "test").save();

        // Test
        assertNotNull(User.connect("test@gmail.com", "test"));
        assertNull(User.connect("test@gmail.com", "badpassword"));
        assertNull(User.connect("tom@gmail.com", "test"));
    }

    @Test
    public void useTheCommentsRelation() {
        // Create a new user and save it
        User test = new User("test@gmail.com", "test", "test").save();

        // Create a new post
        Post testPost = new Post(test, "My first post", "Hello world").save();

        // Post a first comment
        testPost.addComment("Jeff", "Nice post");
        testPost.addComment("Tom", "I knew that !");

        // Count things
        assertEquals(1, User.count());
        assertEquals(1, Post.count());
        assertEquals(2, Comment.count());

        // Retrieve Bob's post
        testPost = Post.find("byAuthor", test).first();
        assertNotNull(testPost);

        // Navigate to comments
        assertEquals(2, testPost.comments.size());
        assertEquals("Jeff", testPost.comments.get(0).author);

        // Delete the post
        testPost.delete();

        // Check that all comments have been deleted
        assertEquals(1, User.count());
        assertEquals(0, Post.count());
        assertEquals(0, Comment.count());
    }

    @Test
    public void fullTest() {
        Fixtures.loadModels("data.yml");

        // Count things
        assertEquals(2, User.count());
        assertEquals(3, Post.count());
        assertEquals(3, Comment.count());

        // Try to connect as users
        assertNotNull(User.connect("test@gmail.com", "test"));
        assertNotNull(User.connect("jeff@gmail.com", "test"));
        assertNull(User.connect("jeff@gmail.com", "badpassword"));
        assertNull(User.connect("tom@gmail.com", "test"));

        // Find all of Bob's posts
        List<Post> testPosts = Post.find("author.email", "test@gmail.com").fetch();
        assertEquals(2, testPosts.size());

        // Find all comments related to Bob's posts
        List<Comment> testComments = Comment.find("post.author.email", "test@gmail.com").fetch();
        assertEquals(3, testComments.size());

        // Find the most recent post
        Post frontPost = Post.find("order by postedAt desc").first();
        assertNotNull(frontPost);
        assertEquals("About the model layer", frontPost.title);

        // Check that this post has two comments
        assertEquals(2, frontPost.comments.size());

        // Post a new comment
        frontPost.addComment("Jim", "Hello guys");
        assertEquals(3, frontPost.comments.size());
        assertEquals(4, Comment.count());
    }
}
