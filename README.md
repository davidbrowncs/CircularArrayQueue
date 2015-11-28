# Circular Array Queue in Java
This is my implementation of a queue using the notion of a circular array.

The general idea is that when adding elements in the queue, they are added to the end, and when removing elements, they are removed from the beginning. Elements that are "removed" are not set back to null, or any other value, they simply remain in the backing array, but are inaccessible because of the use of the head and tail pointers. There is a pointer to the "head" and to the "tail", which are moved when adding/removing, and when each of these pointers reaches the size of the array being used
to actually store the objects, they are reset to 0.

This queue also grows as more elements than the size of the current array are added, resizing the underlying array by a factor of two each time. You are able to tell when the queue is "full", when the head and tail are both pointing at each other, and the size is bigger than 1.

[There are some fairly good images from Google which demonstrate the idea of a Circular Array Queue.](https://www.google.co.uk/search?q=circular+array&espv=2&biw=1600&bih=815&site=webhp&source=lnms&tbm=isch&sa=X&ved=0ahUKEwj748-e6LHJAhVGWRQKHXQmAtoQ_AUIBigB#)

The test cases are backed by [JUnit v4](http://junit.org/), so if you wish to run the test cases you will need JUnit 4 in your build path.