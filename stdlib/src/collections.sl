data List(
    array: string[*],
    size: int32
)

trait Collection(
    new() :: List,
    size(self) :: int32,
    get(self, index: int32) :: string,
    filter(self, predicate: lambda(string) -> bool) :: List,
    push(mut self, value: string) :: void,
)

make List into Collection {
    func new() :: List {
        empty := *[];
        return @List(empty, 0);
    }

    func size(self) :: int32 {
        return self.size;
    }

    func get(self, index: int32) :: string {
        return self.array[index];
    }

    func filter(self, predicate: lambda(string) -> bool) :: List {
        result := List@new();
        for i in 0..self.size {
            if predicate(self.array[i]) {
                result.push(self.array[i]);
            }
        }
        return result;
    }

    func push(mut self, value: string) {
        self.array[self.size] = value;
        self.size = self.size + 1;
    }
}