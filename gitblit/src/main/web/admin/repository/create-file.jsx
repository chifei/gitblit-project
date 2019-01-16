import React from "react";
import PropTypes from "prop-types";
import {Button, Dialog, Form, Input} from "element-react";

export default class CreateFile extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            name: null,
            type: props.type // file: create file;folder: create folder
        };
    }

    createFile() {
        this.onCreate();
    }

    onCancel() {
        this.props.onCancel();
    }

    onCreate() {
        this.props.onCreate();
    }

    changeName(name) {
        this.setState({name});
    }

    render() {
        return (
            <Dialog
                title={"Create " + this.state.type}
                visible={true}
                onCancel={() => this.onCancel()}
            >
                <Dialog.Body>
                    <Form>
                        <Form.Item label="Name" labelWidth="80">
                            <Input value={this.state.name} onChange={value => this.changeName(value)}></Input>
                        </Form.Item>
                    </Form>
                </Dialog.Body>

                <Dialog.Footer className="dialog-footer">
                    <Button onClick={() => this.onCancel()}>Cancel</Button>
                    <Button type="primary" onClick={() => this.createFile()}>Create</Button>
                </Dialog.Footer>
            </Dialog>
        );
    }

}

CreateFile.propTypes = {
    type: PropTypes.string,
    onCancel: PropTypes.func,
    onCreate: PropTypes.func
};
