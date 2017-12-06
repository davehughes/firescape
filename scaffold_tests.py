import os

class JavaFile(object):
    def __init__(self, path):
        self.path = path

    @property
    def test_file_path(self):
        return (self.path
            .replace('src/main/java', 'src/test/java')
            .replace('.java', 'Test.java'))

    @property
    def test_package_name(self):
        return self.package_name

    @property
    def test_class_name(self):
        return file_class_name(self.test_file_path)

    @property
    def package_name(self):
        splitstr = 'src/main/java/'
        split_start_idx = self.path.index(splitstr)
        if split_start_idx < 0:
            raise Exception("Couldn't find split string, can't determine import module name")
        split_end_idx = split_start_idx + len(splitstr)
        return os.path.dirname(self.path[split_end_idx:]).replace('/', '.')

    @property
    def class_name(self):
        return file_class_name(self.path)

    @property
    def import_name(self):
        return '{}.{}'.format(self.package_name, self.class_name)

    def write_scaffold(self, force=False):
        template = '''package {test_package_name};

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import {import_name};

public class {test_class_name} {{

    @Test
    public void test() {{
        // TODO: implement tests here
    }}
}}
'''
        if os.path.exists(self.test_file_path) and not force:
            print("File exists, skipping")
        
        try:
            os.makedirs(os.path.dirname(self.test_file_path))
        except OSError as e:
            if e.errno != 17:
                raise

        with open(self.test_file_path, 'w') as f:
            code = template.format(
                test_package_name=self.test_package_name, 
                import_name=self.import_name,
                test_class_name=self.test_class_name,
                )
            f.write(code)
            # print("TODO: write scaffold -> {}".format(self.test_file_path))
            # print(code)

def file_class_name(path):
    '''
    'src/main/java/com/example/pkg/Blah.java' -> 'Blah'
    '''
    test_filename = os.path.basename(path)
    test_class_name, _ = os.path.splitext(test_filename)
    return test_class_name


paths = [
    'firescape-204b/src/main/java/org/firescape/client/Buffer.java',
]

files = [JavaFile(p) for p in paths]
