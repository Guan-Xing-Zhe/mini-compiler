function bubbleSort(arr, n) {
    let i = 0;
    while (i < n) {
        let j = 0;
        while (j < n - i - 1) {
            if (arr[j] > arr[j + 1]) {
                let temp = arr[j];
                arr[j] = arr[j + 1];
                arr[j + 1] = temp;
            }
            j = j + 1;
        }
        i = i + 1;
    }
}

let a = 5;
let b = 3;
let c = 8;
let d = 1;
let e = 2;

print("Before bubble sort");
// Simple bubble sort using direct variables (arrays not yet supported)
